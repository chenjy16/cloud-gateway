package org.cloud.gateway.netty.filter;
import org.cloud.gateway.message.ZuulMessage;

public interface SyncGatewayFilter<I extends ZuulMessage, O extends ZuulMessage> extends GatewayFilter<I, O>
{
    O apply(I input);
}
