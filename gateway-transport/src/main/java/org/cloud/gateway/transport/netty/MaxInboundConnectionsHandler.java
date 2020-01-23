package org.cloud.gateway.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by cjy on 2020/1/5.
 */
@ChannelHandler.Sharable
public class MaxInboundConnectionsHandler extends ChannelInboundHandlerAdapter
{
    public static final String CONNECTION_THROTTLED_EVENT = "connection_throttled";

    private static final Logger LOG = LoggerFactory.getLogger(MaxInboundConnectionsHandler.class);
    private static final AttributeKey<Boolean> ATTR_CH_THROTTLED = AttributeKey.newInstance("_channel_throttled");

    private final static AtomicInteger connections = new AtomicInteger(0);
    private final int maxConnections;

    public MaxInboundConnectionsHandler(int maxConnections)
    {
        this.maxConnections = maxConnections;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        if (maxConnections > 0) {
            int currentCount = connections.getAndIncrement();

            if (currentCount + 1 > maxConnections) {
                LOG.warn("Throttling incoming connection as above configured max connections threshold of " + maxConnections);
                Channel channel = ctx.channel();
                channel.attr(ATTR_CH_THROTTLED).set(Boolean.TRUE);
                CurrentPassport.fromChannel(channel).add(PassportState.SERVER_CH_THROTTLING);
                channel.close();
                ctx.pipeline().fireUserEventTriggered(CONNECTION_THROTTLED_EVENT);
            }
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (ctx.channel().attr(ATTR_CH_THROTTLED).get() != null) {
            // Discard this msg as channel is in process of being closed.
        }
        else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if (maxConnections > 0) {
            connections.decrementAndGet();
        }

        super.channelInactive(ctx);
    }
}

