package com.alcshare.proxyconfig;

import com.alcshare.proxyconfig.util.JavaVersion;
import com.alcshare.proxyconfig.util.Logging;
import com.btr.proxy.search.ProxySearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.*;
import java.util.List;

/**
 *
 */
public class ProxyManager implements ServletContextListener
{
    private static ProxyManager instance;   // Singleton

    private Config config;

    private ProxyManager() {
        config = Config.load();
        setDefaultProxy();
    }

    public static synchronized ProxyManager instance() {
        if (instance == null) {
            instance = new ProxyManager();
        }
        return instance;
    }

    public void setConfig(Config config) {
        this.config = config;
        setDefaultProxy();
    }

    private void setDefaultProxy() {
        try {
        ProxySelector proxySelector;

        if (config.isAuto()) {
            ProxySearch proxySearch = new ProxySearch();
            proxySearch.setPacCacheSettings(32, 1000*60*15); // Cache 32 urls for up to 15 min.

            proxySearch.addStrategy(ProxySearch.Strategy.OS_DEFAULT);
            proxySelector = proxySearch.getProxySelector();

        } else if (config.isUseProxy()) {
            proxySelector = new ManualProxySelector(config.getHost(), config.getPort());
        } else {
            proxySelector = new NoProxySelector();
        }
        ProxySelector.setDefault(proxySelector);
        } catch (Exception ex) {
            Logging.println("Unexpected error setting default proxy", ex);
            // note that this can happen currently if the PAC file is not available.
            // see issue at https://code.google.com/p/proxy-vole/issues/detail?id=47
        }

        resetAuthenticationCache();
        if (config.isAuthenticated()) {
            Authenticator.setDefault(new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                if (getRequestorType() == RequestorType.PROXY) {
                  return new PasswordAuthentication(config.getUsername(), config.getPassword().toCharArray());
                } else {
                  return super.getPasswordAuthentication();
                }
              }

            });
        } else {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return super.getPasswordAuthentication();
                }


            });
        }

        // if password is provided, then don't allow transparent NTLM Authentication
        useTransparentNTLMAuthentication(!config.isAuthenticated());
    }

    /**
     * Get the proxy host string for the specified URI.
     * @param uri
     * @return proxy host string or an empty string if no proxy is required
     */
    @NotNull
    public String getProxyHostForURI(@Nullable URI uri) {
        Proxy proxy = getProxyForURI(uri);
        SocketAddress address = proxy.address();
        if (address != null && address instanceof InetSocketAddress)  {
            return ((InetSocketAddress) address).getHostString();
        }
        return "";
    }

    /**
     * Gets the proxy port for the specified URI
     * @param uri
     * @return the proxy port or -1 if no proxy is required
     */
    public int getProxyPortForURI(URI uri) {
    Proxy proxy = getProxyForURI(uri);
        SocketAddress address = proxy.address();
        if (address != null && address instanceof InetSocketAddress)  {
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

    // implementation of ServletContextListener
    public void contextInitialized(ServletContextEvent sce)
    {
        setConfig(Config.load());
    }

    public void contextDestroyed(ServletContextEvent sce)
    {

    }

    private void resetAuthenticationCache() {
        AuthCacheValue.setAuthCache(new AuthCacheImpl());
    }

    /**
     * Attempts to set whether NTLM uses transparent authentication (uses windows operator credentials).
     * This will only work if running under a JDK of at least 1.6.0.
     * @param useTransparent
     */
    private void useTransparentNTLMAuthentication(final boolean useTransparent) {
        JavaVersion runtimeVersion = new JavaVersion();
        if (runtimeVersion.isAtLeast(new JavaVersion("1.6.0"))) {
            NTLMTransparentAuthenticationManager manager = new NTLMTransparentAuthenticationManager();
            manager.useTransparentNTLMAuthentication(useTransparent);
        }
    }

}
