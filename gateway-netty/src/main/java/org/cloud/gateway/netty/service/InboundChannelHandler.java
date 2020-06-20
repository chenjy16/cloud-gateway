package org.cloud.gateway.netty.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;
import io.netty.channel.Channel;


public  final class InboundChannelHandler extends ChannelInboundHandlerAdapter {
    private static final AttributeKey<State> ATTR_STATE = AttributeKey.newInstance("_http_body_size_state");

    private static State getOrCreateCurrentState(Channel ch) {
        State state = ch.attr(ATTR_STATE).get();
        if (state == null) {
            state = createNewState(ch);
        }
        return state;
    }

    private static State createNewState(Channel ch) {
        State state = new State();
        ch.attr(ATTR_STATE).set(state);
        return state;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        State state = null;

        // Reset the state as each new inbound request comes in.
        if (msg instanceof HttpRequest) {
            state = createNewState(ctx.channel());
        }

        // Update the inbound body size with this chunk.
        if (msg instanceof HttpContent) {
            if (state == null) {
                state = getOrCreateCurrentState(ctx.channel());
            }
            state.inboundBodySize += ((HttpContent) msg).content().readableBytes();
        }

        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        try {
            super.userEventTriggered(ctx, evt);
        }
        finally {
           /* if (evt instanceof HttpLifecycleChannelHandler.CompleteEvent) {
                ctx.channel().attr(ATTR_STATE).set(null);
            }*/
        }
    }



}
