package org.cloud.gateway.netty.filter;
import org.cloud.gateway.message.GatewayMessage;

public interface SyncGatewayFilter<I extends GatewayMessage, O extends GatewayMessage> extends GatewayFilter<I, O> {
    O apply(I input);
}
