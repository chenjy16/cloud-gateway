package org.cloud.gateway.transport.netty;

import com.netflix.zuul.message.http.HttpRequestMessage;

import com.netflix.zuul.message.http.HttpResponseInfo;

import io.netty.handler.codec.http.Cookie;

public interface HttpResponseMessage extends HttpResponseInfo
{
    void setStatus(int status);

    @Override
    int getMaxBodySize();

    void addSetCookie(Cookie cookie);

    void setSetCookie(Cookie cookie);

    boolean removeExistingSetCookie(String cookieName);

    /** The mutable request that will be sent to Origin. */
    HttpRequestMessage getOutboundRequest();

    /** The immutable response that was received from Origin. */
    HttpResponseInfo getInboundResponse();

    /** This should be called after response received from Origin, to store
     * a copy of the response as-is. */
    void storeInboundResponse();
}
