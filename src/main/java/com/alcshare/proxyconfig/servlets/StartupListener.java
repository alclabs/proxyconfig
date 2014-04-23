package com.alcshare.proxyconfig.servlets;

import com.alcshare.proxyconfig.Config;
import com.alcshare.proxyconfig.ProxyManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 */
public class StartupListener implements ServletContextListener
{
    // implementation of ServletContextListener
    public void contextInitialized(ServletContextEvent sce)
    {
        ProxyManager.instance().setConfig(Config.load());
    }

    public void contextDestroyed(ServletContextEvent sce)
    {

    }
}
