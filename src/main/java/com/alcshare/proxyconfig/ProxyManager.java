package com.alcshare.proxyconfig;

import com.alcshare.proxyconfig.util.JavaVersion;
import com.btr.proxy.search.ProxySearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

import java.net.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public final class ProxyManager {
   private static final ProxyManager instance = new ProxyManager();   // Singleton
   private final ExecutorService delayedLoader = Executors.newCachedThreadPool();

   // disallow instantiation
   private ProxyManager() { }

   public static ProxyManager instance() {
      return instance;
   }

   public void setConfig(final Config config) {
      ProxySelector httpSelector;

      if (config.isAuto())
         httpSelector = new AutoProxySelector(ProxySelector.getDefault(), ProxySearch.Strategy.OS_DEFAULT, 32, 1000 * 60 * 15, delayedLoader); // Cache 32 urls for up to 15 min.
      else if (config.isUseProxy())
         httpSelector = new ManualProxySelector(config.getHost(), config.getPort());
      else
         httpSelector = new NoProxySelector();

      ProxySelector.setDefault(new HttpProxySelectorAdapter(httpSelector, ProxySelector.getDefault()));

      resetAuthenticationCache();

      final PasswordAuthentication passwordAuthentication = config.isAuthenticated() ?
            new PasswordAuthentication(config.getUsername(), config.getPassword().toCharArray()) : null;

      Authenticator.setDefault(new Authenticator() {
         @Override protected PasswordAuthentication getPasswordAuthentication() {
            return getRequestorType() == RequestorType.PROXY ? passwordAuthentication : null;
         }
      });

      // if password is provided, then don't allow transparent NTLM Authentication
      useTransparentNTLMAuthentication(!config.isAuthenticated());
   }

   /**
    * Get the proxy host string for the specified URI.
    *
    * @param uri .
    * @return proxy host string or an empty string if no proxy is required
    */
   @NotNull
   public String getProxyHostForURI(@Nullable URI uri) {
      Proxy proxy = getProxyForURI(uri);
      SocketAddress address = proxy.address();
      if (address != null && address instanceof InetSocketAddress) {
         return ((InetSocketAddress) address).getHostString();
      }
      return "";
   }

   /**
    * Gets the proxy port for the specified URI
    *
    * @param uri .
    * @return the proxy port or -1 if no proxy is required
    */
   public int getProxyPortForURI(URI uri) {
      Proxy proxy = getProxyForURI(uri);
      SocketAddress address = proxy.address();
      if (address != null && address instanceof InetSocketAddress) {
         return ((InetSocketAddress) address).getPort();
      }
      return -1;
   }

   @NotNull
   public Proxy getProxyForURI(@Nullable URI uri) {
      ProxySelector ps = ProxySelector.getDefault();
      if (uri != null) {
         List<Proxy> proxies = ps.select(uri);
         if (proxies != null && proxies.size() > 0) {
            return proxies.get(0);
         }
      }
      return Proxy.NO_PROXY;
   }

   private void resetAuthenticationCache() {
      AuthCacheValue.setAuthCache(new AuthCacheImpl());
   }

   /**
    * Attempts to set whether NTLM uses transparent authentication (uses windows operator credentials).
    * This will only work if running under a JDK of at least 1.6.0.
    *
    * @param useTransparent .
    */
   private void useTransparentNTLMAuthentication(final boolean useTransparent) {
      JavaVersion runtimeVersion = new JavaVersion();
      if (runtimeVersion.isAtLeast(new JavaVersion("1.6.0"))) {
         NTLMTransparentAuthenticationManager manager = new NTLMTransparentAuthenticationManager();
         manager.useTransparentNTLMAuthentication(useTransparent);
      }
   }

   public void shutdown() {
      delayedLoader.shutdownNow();
   }
}