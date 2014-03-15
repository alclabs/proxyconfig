package com.alcshare.proxyconfig;

import com.alcshare.proxyconfig.util.AESUtil;
import com.alcshare.proxyconfig.util.AddOnInfoWrapper;
import com.alcshare.proxyconfig.util.Logging;
import com.controlj.green.addonsupport.access.*;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class Config
{
    private static final String PROP_AUTO = "auto";
    private static final String PROP_USEPROXY = "useproxy";
    private static final String PROP_HOST = "host";
    private static final String PROP_PORT = "port";
    private static final String PROP_TESTURL = "testurl";
    private static final String PROP_AUTHREQ = "authreq";
    private static final String PROP_USERNAME = "username";
    private static final String PROP_PASSWORD = "password";

    private static final String STORE_NAME = "proxyconfig";

    private static final String DEFAULT_PORT_STRING = "";
    private static final String DEFAULT_TESTURL = "www.google.com";

    private Properties props;

    private Config(Properties props) {
        this.props = props;
    }

    @NotNull
    public static Config load()
    {
        SystemConnection connection = new AddOnInfoWrapper().getRootSystemConnection();
        Properties props = null;
        try
        {
            props = connection.runReadAction(new ReadActionResult<Properties>()
            {
                public Properties execute(@NotNull SystemAccess access) throws Exception
                {
                    DataStore store = access.getSystemDataStore(STORE_NAME);
                    Properties props = new Properties(getDefaultProperties());
                    try {
                        props.load(store.getReader());
                    } catch (IOException ex) {}
                    return props;
                }
            });
        } catch (Exception e) {
            Logging.println("Error loading proxy configuration.  Using defaults!", e);
            // use defaults from below
        }

        return new Config(props);
    }

    public void save() throws IOException
    {
        SystemConnection connection = new AddOnInfoWrapper().getRootSystemConnection();
        try
        {
            connection.runWriteAction("Saving new proxy configuration", new WriteAction() {

                public void execute(@NotNull WritableSystemAccess access) throws Exception
                {
                    DataStore store = access.getSystemDataStore(STORE_NAME);
                    props.store(store.getWriter(), null);
                }
            });
        } catch (Exception e)  {
            Logging.println("Error saving proxy configuration!", e);
            throw new IOException("Error saving proxy configuration", e);
        }
    }

    public boolean isAuto() {
        return Boolean.parseBoolean(props.getProperty(PROP_AUTO));
    }

    public void setAuto(boolean auto) {
        props.setProperty(PROP_AUTO, new Boolean(auto).toString());
    }

    public boolean isUseProxy() {
        return Boolean.parseBoolean(props.getProperty(PROP_USEPROXY));
    }

    public void setUseProxy(boolean useProxy) {
        props.setProperty(PROP_USEPROXY, new Boolean(useProxy).toString());
    }

    @NotNull
    public String getHost() {
        return props.getProperty(PROP_HOST);
    }

    public void setHost(@NotNull String host) {
        props.setProperty(PROP_HOST, host);
    }

    public int getPort() {
        String portString = props.getProperty(PROP_PORT, DEFAULT_PORT_STRING);
        try
        {
            return Integer.parseInt(portString);
        } catch (NumberFormatException e)
        {
            Logging.println("Illegal port number in serialized config", e);
            return Integer.parseInt(DEFAULT_PORT_STRING);
        }
    }

    public void setPort(@NotNull String portString) throws IllegalArgumentException {
        int port;
        try
        {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("'"+portString+"' is not a valid port number", e);
        }
        if (port <= 0) {
            throw new IllegalArgumentException("'"+portString+"' is not a valid port number");
        }
        props.setProperty(PROP_PORT, Integer.toString(port));
    }

    public boolean isAuthenticated() {
        return Boolean.parseBoolean(props.getProperty(PROP_AUTHREQ));
    }

    public void setAuthenticated(boolean authenticated) {
        props.setProperty(PROP_AUTHREQ, new Boolean(authenticated).toString());
    }

    @NotNull
    public String getUsername() {
        return props.getProperty(PROP_USERNAME);
    }

    public void setUsername(@NotNull String username) {
        props.setProperty(PROP_USERNAME, username);
    }

    @NotNull
    public String getPassword() {
        return AESUtil.decrypt(props.getProperty(PROP_PASSWORD));
    }

    public void setPassword(@NotNull String password) {
        props.setProperty(PROP_PASSWORD, AESUtil.encrypt(password));
    }


    @NotNull
    public String getTestURL() {
        return props.getProperty(PROP_TESTURL);
    }

    public void setTestURL(@NotNull String testURL) {
        props.setProperty(PROP_TESTURL, testURL);
    }


    private static Properties getDefaultProperties() {
        Properties result = new Properties();
        result.setProperty(PROP_AUTO, "true");
        result.setProperty(PROP_USEPROXY, "true");
        result.setProperty(PROP_HOST, "");
        result.setProperty(PROP_PORT, DEFAULT_PORT_STRING);
        result.setProperty(PROP_TESTURL, DEFAULT_TESTURL);
        result.setProperty(PROP_AUTHREQ, "false");
        result.setProperty(PROP_USERNAME, "");
        result.setProperty(PROP_PASSWORD, "");
        return result;
    }
}
