package org.cloud.gateway.netty.service;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;

public  final class OutboundChannelHandler extends ChannelOutboundHandlerAdapter {


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
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        State state = null;

        // Reset the state as each new outbound request goes out.
        if (msg instanceof HttpRequest) {
            state = createNewState(ctx.channel());
        }

        // Update the outbound body size with this chunk.
        if (msg instanceof HttpContent) {
            if (state == null) {
                state = getOrCreateCurrentState(ctx.channel());
            }
            state.outboundBodySize += ((HttpContent) msg).content().readableBytes();
        }
        super.write(ctx, msg, promise);
    }

}
