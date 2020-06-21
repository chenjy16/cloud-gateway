package org.cloud.gateway.netty.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import javax.inject.Singleton;

@Singleton
public class SessionContextDecorator {



    public SessionContext decorate(SessionContext ctx) {
        // TODO split out commons parts from BaseSessionContextDecorator
        ChannelHandlerContext nettyCtx = (ChannelHandlerContext) ctx.get("");
        if (nettyCtx == null) {
            return null;
        }
        Channel channel = nettyCtx.channel();
        // Providers for getting the size of read/written request and response body sizes from channel.
        ctx.set("", HttpBodySizeRecordingChannelHandler.getCurrentInboundBodySize(channel));
        ctx.set("", HttpBodySizeRecordingChannelHandler.getCurrentOutboundBodySize(channel));
        //ctx.setUUID(UUID_FACTORY.generateRandomUuid().toString());
        return ctx;
    }
}

