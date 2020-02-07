package org.cloud.gateway.transport.netty;



import io.netty.handler.codec.http.HttpResponse;

public class Http1ConnectionExpiryHandler extends AbstrHttpConnectionExpiryHandler
{
    public Http1ConnectionExpiryHandler(int maxRequests, int maxRequestsUnderBrownout, int maxExpiry)
    {
        super(ConnectionCloseType.GRACEFUL, maxRequestsUnderBrownout, maxRequests, maxExpiry);
    }

    @Override
    protected boolean isResponseHeaders(Object msg)
    {
        return msg instanceof HttpResponse;
    }
}
