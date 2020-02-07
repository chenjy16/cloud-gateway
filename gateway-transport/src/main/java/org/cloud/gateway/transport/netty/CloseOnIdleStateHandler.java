package org.cloud.gateway.transport.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class CloseOnIdleStateHandler extends ChannelInboundHandlerAdapter
{
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {
            ctx.close();
        }
    }
}