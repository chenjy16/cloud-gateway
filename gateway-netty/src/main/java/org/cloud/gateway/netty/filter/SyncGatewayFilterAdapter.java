package org.cloud.gateway.netty.filter;

import org.cloud.gateway.message.ZuulMessage;

public abstract class SyncGatewayFilterAdapter<I extends ZuulMessage, O extends ZuulMessage> implements SyncGatewayFilter<I, O> {


}
