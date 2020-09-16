package org.cloud.gateway.netty.filter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.cloud.gateway.connections.BasicNettyOrigin;
import org.cloud.gateway.connections.OriginResponseReceiver;
import org.cloud.gateway.connections.PooledConnection;
import org.cloud.gateway.connections.Server;
import org.cloud.gateway.message.HttpRequestMessage;
import org.cloud.gateway.message.HttpResponseMessage;

import java.util.concurrent.atomic.AtomicReference;


public class ProxyEndpoint  extends SyncGatewayFilterAdapter<HttpRequestMessage, HttpResponseMessage> implements GenericFutureListener<Future<PooledConnection>> {



    private  FilterRunner responseFilters;
    private volatile OriginResponseReceiver originResponseReceiver;
    private final ChannelHandlerContext channelCtx;
    protected final HttpRequestMessage zuulRequest;

    protected final AtomicReference<Server> chosenServer;
    protected final AtomicReference<String> chosenHostAddr;
    protected final BasicNettyOrigin origin;
    protected int attemptNum;
    protected MethodBinding<?> methodBinding;






    public ProxyEndpoint(final HttpRequestMessage inMesg, final ChannelHandlerContext ctx,
                         final FilterRunner<HttpResponseMessage, ?> filters, MethodBinding<?> methodBinding) {
        channelCtx = ctx;
        responseFilters = filters;
        this.methodBinding = methodBinding;
        this.zuulRequest = null;
        this.chosenServer = null;
        this.chosenHostAddr = null;
        this.origin = null;


    }




    @Override
    public void operationComplete(Future<PooledConnection> pooledConnectionFuture) throws Exception {

    }



    public HttpResponseMessage apply(final HttpRequestMessage input) {

        // If no Origin has been selected, then just return a 404 static response.
        // handle any exception here
        try {

            //代理请求到后端服务。这里就不详细分析了，不然写不完了
            proxyRequestToOrigin();

            //Doesn't return origin response to caller, calls invokeNext() internally in response filter chain
            return null;
        } catch (Exception ex) {

            return null;
        }

    }



    private void proxyRequestToOrigin() {
        Promise<PooledConnection> promise = origin.connectToOrigin(zuulRequest, channelCtx.channel().eventLoop(), attemptNum, chosenServer, chosenHostAddr);
       // writeClientRequestToOrigin(conn, readTimeout);
    }



    private void writeClientRequestToOrigin(final PooledConnection conn, int readTimeout) {
        final Channel ch = conn.getChannel();
        //ch.attr(ClientTimeoutHandler.ORIGIN_RESPONSE_READ_TIMEOUT).set(readTimeout);

        //返回给前端
        final ChannelPipeline pipeline = ch.pipeline();
        originResponseReceiver = getOriginResponseReceiver();
        pipeline.addBefore("connectionPoolHandler", OriginResponseReceiver.CHANNEL_HANDLER_NAME, originResponseReceiver);


        ch.write(zuulRequest);
        writeBufferedBodyContent(zuulRequest, ch);
        ch.flush();
        //Get ready to read origin's response
        ch.read();

    }



    private static void writeBufferedBodyContent(final HttpRequestMessage zuulRequest, final Channel channel) {
        zuulRequest.getBodyContents().forEach((chunk) -> {
            channel.write(chunk.retain());
        });
    }





    OriginResponseReceiver getOriginResponseReceiver() {
        return new OriginResponseReceiver(this);
    }




    @Override
    public FilterSyncType getSyncType() {
        return null;
    }

    @Override
    public HttpResponseMessage getDefaultOutput(HttpRequestMessage input) {
        return null;
    }
}
