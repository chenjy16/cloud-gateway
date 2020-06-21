package org.cloud.gateway.netty.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;

public class ClientResponseWriter extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        final Channel channel = ctx.channel();


        if (msg instanceof HttpContent) {
            final HttpContent chunk = (HttpContent) msg;
            if (channel.isActive()) {
                channel.writeAndFlush(chunk);
            } else {
                chunk.release();
                channel.close();
            }
        }
        else {


        }
    }



}
