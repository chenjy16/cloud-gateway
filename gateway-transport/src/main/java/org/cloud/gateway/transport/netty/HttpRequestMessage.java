package org.cloud.gateway.transport.netty;


public interface HttpRequestMessage extends HttpRequestInfo
{
    void setProtocol(String protocol);

    void setMethod(String method);

    void setPath(String path);

    void setScheme(String scheme);

    void setServerName(String serverName);

    ZuulMessage clone();

    void storeInboundRequest();

    HttpRequestInfo getInboundRequest();

    void setQueryParams(HttpQueryParams queryParams);
}
