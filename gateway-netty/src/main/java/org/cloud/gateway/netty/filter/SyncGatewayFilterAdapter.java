package org.cloud.gateway.netty.filter;


import org.cloud.gateway.message.GatewayMessage;

public abstract class SyncGatewayFilterAdapter<I extends GatewayMessage, O extends GatewayMessage> implements SyncGatewayFilter<I, O> {


}
