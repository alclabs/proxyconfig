/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2015 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)BaseHttpProxySelector

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.proxyconfig;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

public class HttpProxySelectorAdapter extends ProxySelector {
   private final ProxySelector httpSelector;
   private final ProxySelector otherSelector;

   public HttpProxySelectorAdapter(ProxySelector httpSelector, ProxySelector otherSelector) {
      this.httpSelector = httpSelector;
      this.otherSelector = otherSelector;
   }

   @Override public List<Proxy> select(URI uri) {
      if (isHttp(uri))
         return httpSelector.select(uri);
      else
         return otherSelector.select(uri);
   }

   @Override public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
      if (isHttp(uri))
         httpSelector.connectFailed(uri, sa, ioe);
      else
         otherSelector.connectFailed(uri, sa, ioe);
   }

   private boolean isHttp(URI uri) {
      String scheme = uri.getScheme();
      return "http".equals(scheme) || "https".equals(scheme);
   }
}

