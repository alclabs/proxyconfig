package com.alcshare.proxyconfig;

import sun.net.www.protocol.http.ntlm.NTLMAuthenticationCallback;

import java.net.URL;

/**
 *
 */
public class NTLMTransparentAuthenticationManager
{
    public void useTransparentNTLMAuthentication(final boolean useTransparent) {
        NTLMAuthenticationCallback.setNTLMAuthenticationCallback(new NTLMAuthenticationCallback()
        {
            @Override
            public boolean isTrustedSite(URL url)
            {
                return !useTransparent;
            }
        });

    }
}
