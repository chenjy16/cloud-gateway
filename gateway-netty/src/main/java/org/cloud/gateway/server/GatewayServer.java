package org.cloud.gateway.server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpServerCodec;
import org.cloud.gateway.handler.GatewayFilterChainHandler;
import org.cloud.gateway.netty.filter.GatewayFilterChainRunner;
import org.cloud.gateway.netty.service.*;


public final class GatewayServer extends NettyTCPServer {


    SessionContextDecorator sessionCtxDecorator;
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
       // pipeline.addLast(new HttpServerLifecycleChannelHandler.HttpServerLifecycleInboundChannelHandler());
       //pipeline.addLast(new HttpServerLifecycleChannelHandler.HttpServerLifecycleOutboundChannelHandler());
       // pipeline.addLast(new HttpBodySizeRecordingChannelHandler.InboundChannelHandler());
       // pipeline.addLast(new HttpBodySizeRecordingChannelHandler.OutboundChannelHandler());
        //pipeline.addLast(getChannelHandler());
        pipeline.addLast(new ClientRequestReceiver(sessionCtxDecorator));
        addZuulFilterChainHandler(pipeline);
        pipeline.addLast(new ClientResponseWriter());

    }


     void addZuulFilterChainHandler(final ChannelPipeline pipeline) {
         // response filter chain  包含所有的处理响应的filter，可以通过配置文件指定
         final GatewayFilterChainRunner responseFilterChain = null;
         // request filter chain | end point | response filter chain   所以的请求filter 管道。一个一个执行，requestFilterChain里面引用了endPoint
         final GatewayFilterChainRunner requestFilterChain = null;


         pipeline.addLast(new GatewayFilterChainHandler(requestFilterChain, responseFilterChain));

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
