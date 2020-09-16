package org.cloud.gateway.netty.service;

import io.netty.handler.codec.Headers;

import java.net.URLDecoder;

public class HttpRequestMessageImpl {

    private final boolean immutable;
    private ZuulMessage message;
    private String protocol;
    private String method;
    private String path;
    private String decodedPath;
    private HttpQueryParams queryParams;
    private String clientIp;
    private String scheme;
    private int port;
    private String serverName;


    public HttpRequestMessageImpl(SessionContext context, String protocol, String method, String path,
                                  HttpQueryParams queryParams, Headers headers, String clientIp, String scheme,
                                  int port, String serverName,
                                  boolean immutable)
    {
        this.immutable = immutable;
        this.message = new GatewayMessageImpl(context, headers);
        this.protocol = protocol;
        this.method = method;
        this.path = path;
        try {
            this.decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            // fail to decode URI
            // just set decodedPath to original path
            this.decodedPath = path;
        }
        // Don't allow this to be null.
        this.queryParams = queryParams == null ? new HttpQueryParams() : queryParams;
        this.clientIp = clientIp;
        this.scheme = scheme;
        this.port = port;
        this.serverName = serverName;
    }

}
