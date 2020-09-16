package org.cloud.gateway.netty.filter;
import org.cloud.gateway.message.ZuulMessage;

public interface GatewayFilter<I extends ZuulMessage, O extends ZuulMessage> {

    FilterSyncType getSyncType();

    O getDefaultOutput(I input);
}
