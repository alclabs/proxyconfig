package com.alcshare.proxyconfig;

import com.alcshare.proxyconfig.util.Logging;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class ManualProxySelector extends ProxySelector
{
    private List<Proxy> proxies;

    public ManualProxySelector(String host, int port) {
        // todo - should this have DIRECT as a second entry?
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        List<Proxy> plist = new ArrayList<Proxy>();
        plist.add(proxy);
        proxies = Collections.unmodifiableList(plist);
    }

    @Override
    public List<Proxy> select(URI uri)
    {
        return proxies;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe)
    {
        Logging.println("note - connection failed to '"+uri+"' using proxy at:"+sa);
    }
}
