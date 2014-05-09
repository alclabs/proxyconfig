package com.alcshare.proxyconfig.servlets;

import com.alcshare.proxyconfig.Config;
import com.alcshare.proxyconfig.ProxyManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.ProxySelector;

/**
 *
 */
public class StartupListener implements ServletContextListener
{
    static ProxySelector defaultProxySelector;

    // implementation of ServletContextListener
    public void contextInitialized(ServletContextEvent sce)
    {
        defaultProxySelector = ProxySelector.getDefault();
        ProxyManager.instance().setConfig(Config.load());
    }

    public void contextDestroyed(ServletContextEvent sce)
    {
        // unset the default proxy selector
        ProxySelector.setDefault(defaultProxySelector);
    }
}
