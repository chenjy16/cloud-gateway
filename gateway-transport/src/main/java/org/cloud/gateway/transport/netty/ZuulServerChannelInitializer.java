package org.cloud.gateway.transport.netty;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;

public class ZuulServerChannelInitializer extends BaseZuulChannelInitializer {

    public ZuulServerChannelInitializer(
            String metricId,
            ChannelConfig channelConfig,
            ChannelConfig channelDependencies,
            ChannelGroup channels) {
        super(metricId, channelConfig, channelDependencies, channels);
    }

    /**
     * Use {@link #ZuulServerChannelInitializer(String, ChannelConfig, ChannelConfig, ChannelGroup)} instead.
     */
    @Deprecated
    public ZuulServerChannelInitializer(
            int port,
            ChannelConfig channelConfig,
            ChannelConfig channelDependencies,
            ChannelGroup channels) {
        this(String.valueOf(port), channelConfig, channelDependencies, channels);
    }

    @Override
    protected void initChannel(Channel ch) throws Exception
    {
        // Configure our pipeline of ChannelHandlerS.
        ChannelPipeline pipeline = ch.pipeline();    
        storeChannel(ch);
        addTimeoutHandlers(pipeline);
        //addPassportHandler(pipeline);
        //addTcpRelatedHandlers(pipeline);
        addHttp1Handlers(pipeline);
        addHttpRelatedHandlers(pipeline);
        addZuulHandlers(pipeline);
    }
}
