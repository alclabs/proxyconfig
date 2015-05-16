package com.alcshare.proxyconfig;

import com.alcshare.proxyconfig.util.Logging;
import com.btr.proxy.search.ProxySearch;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class AutoProxySelector extends ProxySelector {
   private final ProxySelector defaultSelector;
   private final Future<ProxySelector> selectorFuture;
   private final AtomicReference<ProxySelector> delegateSelectorRef = new AtomicReference<>();

   // this variable is used to prevent the DeferredProxySearch from being blocked by waitForSelector() when it is
   // looking for the PAC file.
   private static final ThreadLocal<Boolean> isDeferredProxySearchThread = new ThreadLocal<>();

   public AutoProxySelector(ProxySelector defaultSelector, ProxySearch.Strategy strategy, int cacheSize, int cacheTtl, ExecutorService delayedLoader) {
      this.defaultSelector = defaultSelector;
      selectorFuture = delayedLoader.submit(new DeferredProxySearch(strategy, cacheSize, cacheTtl));
   }

   @Override
   public List<Proxy> select(URI uri) {
      return waitForSelector().select(uri);
   }

   @Override
   public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
      ProxySelector selector = delegateSelectorRef.get();
      if (selector != null)
         selector.connectFailed(uri, sa, ioe);
      else
         Logging.println("auto proxy failed to initialize correctly");
   }

   private ProxySelector waitForSelector() {
      ProxySelector selector = delegateSelectorRef.get();
      if (selector == null) {
         if (isDeferredProxySearchThread.get() == Boolean.TRUE)
            return defaultSelector;

         try {
            selector = selectorFuture.get();
         } catch (InterruptedException e) {
            selector = defaultSelector;
         } catch (ExecutionException e) {
            selector = defaultSelector;
         }

         if (selector == null)
            selector = new NoProxySelector();

         delegateSelectorRef.set(selector);
      }
      return selector;
   }

   private class DeferredProxySearch implements Callable<ProxySelector> {
      private final ProxySearch.Strategy strategy;
      private final int cacheSize;
      private final int cacheTtl;

      private DeferredProxySearch(ProxySearch.Strategy strategy, int cacheSize, int cacheTtl) {
         this.strategy = strategy;
         this.cacheSize = cacheSize;
         this.cacheTtl = cacheTtl;
      }

      public ProxySelector call() throws Exception {
         try {
            isDeferredProxySearchThread.set(Boolean.TRUE);
            ProxySearch proxySearch = new ProxySearch();
            proxySearch.setPacCacheSettings(cacheSize, cacheTtl);
            proxySearch.addStrategy(strategy);
            return proxySearch.getProxySelector();
         } catch (Exception e) {
            Logging.println("Unexpected error setting default proxy", e);
            // note that this can happen currently if the PAC file is not available.
            // see issue at https://code.google.com/p/proxy-vole/issues/detail?id=47
            throw e;
         } finally {
            isDeferredProxySearchThread.remove();
         }
      }
   }
}
