package org.cloud.gateway.netty.service;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;



public final class GatewayServer extends NettyTCPServer{




    public GatewayServer(int port) {
        super(port);
    }


    public GatewayServer(int port, String host) {
        super(port,host);
    }




    @Override
    public EventLoopGroup getBossGroup() {
        return getBossGroup();
    }

    @Override
    public EventLoopGroup getWorkerGroup() {
        return getWorkerGroup();
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        //pipeline.addLast(new HttpObjectAggregator(65536));
        //pipeline.addLast(getChannelHandler());
    }

    @Override
    protected void initOptions(ServerBootstrap b) {
        super.initOptions(b);
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.childOption(ChannelOption.SO_SNDBUF, 32 * 1024);
        b.childOption(ChannelOption.SO_RCVBUF, 32 * 1024);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return null;
    }

}
