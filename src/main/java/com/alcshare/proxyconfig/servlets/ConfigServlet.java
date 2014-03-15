package com.alcshare.proxyconfig.servlets;

import com.alcshare.proxyconfig.Config;
import com.alcshare.proxyconfig.ProxyManager;
import com.alcshare.proxyconfig.util.Logging;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;

/**
 *
 */
public class ConfigServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        try {
            writeResults(resp);
        } catch (IOException ex) {
            Logging.println("Error writing response from ConfigServlet", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String autoString = req.getParameter("auto");
        boolean auto = Boolean.parseBoolean(autoString);
        boolean useProxy = Boolean.parseBoolean(req.getParameter("useproxy"));
        String host = req.getParameter("host");
        String portString = req.getParameter("port");
        String testurl = req.getParameter("testurl");
        String authReqString = req.getParameter("authreq");
        boolean authReq = Boolean.parseBoolean(authReqString);
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Config config = Config.load();
        config.setAuto(auto);
        config.setUseProxy(useProxy);
        config.setHost(host);
        if (portString.length() > 0) {
            try {
                config.setPort(portString);
            } catch (IllegalArgumentException ex) {
                Logging.println("invalid port value in posted data: '"+portString+"'", ex);
            }
        }

        config.setAuthenticated(authReq);
        config.setUsername(username);
        config.setPassword(password);

        config.setTestURL(testurl);
        try {
            config.save();
        } catch (IOException ex) {
            //todo - add error handling in JS
            Logging.println("Error saving configuration", ex);
        }
        ProxyManager.instance().setConfig(config);

        try {
            writeResults(resp);
        } catch (IOException ex) {
            Logging.println("Error writing response after post from ConfigServlet", ex);
        }
    }

    private void writeResults(HttpServletResponse resp) throws IOException
    {
        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/json");
        JSONObject result = new JSONObject();

        Config config = Config.load();
        ProxyManager mgr = ProxyManager.instance();
        String testURL = config.getTestURL();
        URI testURI = null;
        try {
            testURI = new URI(ProxyTestServlet.PROTOCOL_PREFIX+testURL);
        } catch (URISyntaxException e)
        {
            Logging.println("Test URL '"+testURL+"' is not a valid URL");
        }

        result.put("auto", config.isAuto());

        boolean useProxy = config.isUseProxy();
        if (config.isAuto()) {
            Proxy proxy = mgr.getProxyForURI(testURI);
            useProxy = proxy.type() != Proxy.Type.DIRECT;
        }
        result.put("useproxy", useProxy);
        result.put("host", mgr.getProxyHostForURI(testURI));
        result.put("port", mgr.getProxyPortForURI(testURI));

        result.put("authreq", config.isAuthenticated());
        result.put("username", config.getUsername());
        result.put("password", config.getPassword());

        result.put("testurl", testURL);

        result.write(writer);
    }
}
