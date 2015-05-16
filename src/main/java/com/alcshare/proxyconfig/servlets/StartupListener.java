package com.alcshare.proxyconfig.servlets;

import com.alcshare.proxyconfig.Config;
import com.alcshare.proxyconfig.ProxyManager;
import sun.net.www.protocol.http.ntlm.NTLMAuthenticationCallback;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.Authenticator;
import java.net.ProxySelector;

/**
 *
 */
public class StartupListener implements ServletContextListener {
   private static ProxySelector defaultProxySelector;
   private static NTLMAuthenticationCallback defaultNtlmAuthenticationCallback;

   // implementation of ServletContextListener
   public void contextInitialized(ServletContextEvent sce) {
      defaultProxySelector = ProxySelector.getDefault();
      defaultNtlmAuthenticationCallback = NTLMAuthenticationCallback.getNTLMAuthenticationCallback();
      ProxyManager.instance().setConfig(Config.load());
   }

   public void contextDestroyed(ServletContextEvent sce) {
      // unset system-wide settings so that we revert back to "unchanged" behavior, and so our WebAppClassLoader can
      // be garbage collected
      ProxySelector.setDefault(defaultProxySelector);
      Authenticator.setDefault(null);
      NTLMAuthenticationCallback.setNTLMAuthenticationCallback(defaultNtlmAuthenticationCallback);
      ProxyManager.instance().shutdown();
   }
}
