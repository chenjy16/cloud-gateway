package org.cloud.gateway.transport.netty;

import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.message.http.Cookies;
import com.netflix.zuul.message.http.HttpRequestInfo;

public interface HttpResponseInfo extends ZuulMessage
{
    int getStatus();

    /** The immutable request that was originally received from client. */
    HttpRequestInfo getInboundRequest();

    @Override
    ZuulMessage clone();

    @Override
    String getInfoForLogging();

    Cookies parseSetCookieHeader(String setCookieValue);
    boolean hasSetCookieWithName(String cookieName);
}

