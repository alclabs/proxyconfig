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
public class NoProxySelector extends ProxySelector
{
    private final List<Proxy> proxies;

    public NoProxySelector() {
        List<Proxy> plist = new ArrayList<>();
        plist.add(Proxy.NO_PROXY);
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
