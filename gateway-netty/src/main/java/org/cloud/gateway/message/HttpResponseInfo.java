package org.cloud.gateway.message;

import reactor.netty.http.Cookies;

public interface HttpResponseInfo extends ZuulMessage {
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
