package org.cloud.gateway.netty.filter;
import org.cloud.gateway.message.GatewayMessage;

public interface GatewayFilter<I extends GatewayMessage, O extends GatewayMessage> {

    FilterSyncType getSyncType();

    O getDefaultOutput(I input);
}
