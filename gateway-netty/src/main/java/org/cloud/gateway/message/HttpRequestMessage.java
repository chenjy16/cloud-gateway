package org.cloud.gateway.message;

import org.cloud.gateway.netty.service.HttpQueryParams;

public interface HttpRequestMessage extends HttpRequestInfo
{
    void setProtocol(String protocol);

    void setMethod(String method);

    void setPath(String path);

    void setScheme(String scheme);

    void setServerName(String serverName);

    GatewayMessage clone();

    void storeInboundRequest();

    HttpRequestInfo getInboundRequest();

    void setQueryParams(HttpQueryParams queryParams);
}
