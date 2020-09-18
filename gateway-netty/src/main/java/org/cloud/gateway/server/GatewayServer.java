package org.cloud.gateway.server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpServerCodec;
import org.cloud.gateway.handler.ClientRequestReceiverHandler;
import org.cloud.gateway.handler.ClientResponseWriterHandler;
import org.cloud.gateway.handler.GatewayFilterChainHandler;
import org.cloud.gateway.handler.RequestCompleteHandler;
import org.cloud.gateway.message.HttpRequestMessage;
import org.cloud.gateway.message.HttpResponseMessage;
import org.cloud.gateway.netty.filter.FilterRunner;
import org.cloud.gateway.netty.filter.GatewayFilter;
import org.cloud.gateway.netty.filter.GatewayFilterChainRunner;
import org.cloud.gateway.netty.service.*;


public final class GatewayServer extends NettyTCPServer {


    private RequestCompleteHandler requestCompleteHandler;
    private SessionContextDecorator sessionCtxDecorator;
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

        pipeline.addLast(new ClientRequestReceiverHandler(sessionCtxDecorator));
        addZuulFilterChainHandler(pipeline);
        pipeline.addLast(new ClientResponseWriterHandler(requestCompleteHandler));

    }



     /**
      * @desc   request filter chain | end point | response filter chain   所以的请求filter 管道。一个一个执行，requestFilterChain里面引用了endPoint
      * @author chenjianyu944@gmail.com
      * @date   2020/9/18 15:35
      **/
     void addZuulFilterChainHandler(final ChannelPipeline pipeline) {

         // response filter chain  包含所有的处理响应的filter，可以通过配置文件指定
         final GatewayFilter<HttpResponseMessage, HttpResponseMessage>[] responseFilters =null;
         final GatewayFilterChainRunner responseFilterChain = new GatewayFilterChainRunner<>(responseFilters);;


         // endpoint | response filter chain
         final FilterRunner<HttpRequestMessage, HttpResponseMessage> endPoint =null;
         final GatewayFilter<HttpRequestMessage, HttpRequestMessage>[] requestFilters =null;
         final GatewayFilterChainRunner<HttpRequestMessage> requestFilterChain = new GatewayFilterChainRunner<>(endPoint,requestFilters);
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
