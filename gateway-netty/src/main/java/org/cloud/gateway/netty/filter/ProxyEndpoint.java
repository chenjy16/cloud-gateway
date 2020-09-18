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
import org.cloud.gateway.netty.service.SessionContext;
import java.util.concurrent.atomic.AtomicReference;


public class ProxyEndpoint  extends SyncGatewayFilterAdapter<HttpRequestMessage, HttpResponseMessage> implements GenericFutureListener<Future<PooledConnection>> {


    @Override
    public String filterName() {
        return "ProxyEndpoint";
    }

    @Override
    public HttpResponseMessage getDefaultOutput(HttpRequestMessage input) {
        return null;
    }



    private  FilterRunner responseFilters;
    private volatile OriginResponseReceiver originResponseReceiver;
    private final ChannelHandlerContext channelCtx;
    protected final HttpRequestMessage request;



    protected final AtomicReference<Server> chosenServer;
    protected final AtomicReference<String> chosenHostAddr;
    protected final BasicNettyOrigin origin;
    protected int attemptNum;
    protected MethodBinding<?> methodBinding;
    protected final SessionContext context;





    public ProxyEndpoint(final HttpRequestMessage inMesg, final ChannelHandlerContext ctx,
                         final FilterRunner<HttpResponseMessage, ?> filters, MethodBinding<?> methodBinding) {
        channelCtx = ctx;
        responseFilters = filters;
        this.methodBinding = methodBinding;
        this.request = null;
        this.chosenServer = null;
        this.chosenHostAddr = null;
        this.origin = null;
        this.context=null;


    }


    /**
     * @desc  代理请求到后端服务
     * @author chenjianyu944@gmail.com
     * @date   2020/9/16 18:51
     **/
    public HttpResponseMessage apply(final HttpRequestMessage input) {

        // If no Origin has been selected, then just return a 404 static response.
        // handle any exception here
        try {
            proxyRequestToOrigin();
            //Doesn't return origin response to caller, calls invokeNext() internally in response filter chain
            return null;
        } catch (Exception ex) {
            //handleError(ex);
            return null;
        }

    }

    
    
    
    /**
     * @desc   
     * @author chenjianyu944@gmail.com
     * @date   2020/9/16 19:52
     **/
    @Override
    public void operationComplete(Future<PooledConnection> connectResult) throws Exception {

        try {
            methodBinding.bind(() -> {
                Integer readTimeout = null;
                Server server = chosenServer.get();
                // Handle the connection establishment result.
                if (connectResult.isSuccess()) {
                    //发送请求
                    if (context.isCancelled()) {
                        // conn isn't actually busy so we can put it in the pool
                        connectResult.getNow().setConnectionState(PooledConnection.ConnectionState.WRITE_READY);
                        connectResult.getNow().release();
                    }else {
                        // Start sending the request to origin now.
                        writeClientRequestToOrigin(connectResult.getNow(), readTimeout);
                    }

                } else {
                    if (!context.isCancelled()) {
                        //errorFromOrigin(connectResult.cause());
                    }
                }
            });
        } catch (Throwable ex) {
            // Fire exception here to ensure that server channel gets closed, so clients don't hang.
            channelCtx.fireExceptionCaught(ex);
        }

    }




    @Override
    public boolean needsBodyBuffered(HttpRequestMessage input) {
        return false;
    }

    
    



    /**
     * @desc   
     * @author chenjianyu944@gmail.com
     * @date   2020/9/16 19:42
     **/
    private void proxyRequestToOrigin() {
        // We pass this AtomicReference<Server> here and the origin impl will assign the chosen server to it.
        Promise<PooledConnection> promise = origin.connectToOrigin(request, channelCtx.channel().eventLoop(), attemptNum, chosenServer, chosenHostAddr);


        try {
            if (promise.isDone()) {
                operationComplete(promise);
            } else {
                promise.addListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     * @desc   
     * @author chenjianyu944@gmail.com
     * @date   2020/9/16 19:53
     **/
    private void writeClientRequestToOrigin(final PooledConnection conn, int readTimeout) {

        final Channel ch = conn.getChannel();
        // set read timeout on origin channel
        //ch.attr(ClientTimeoutHandler.ORIGIN_RESPONSE_READ_TIMEOUT).set(readTimeout);
        final ChannelPipeline pipeline = ch.pipeline();
        originResponseReceiver = getOriginResponseReceiver();
        pipeline.addBefore("connectionPoolHandler", OriginResponseReceiver.CHANNEL_HANDLER_NAME, originResponseReceiver);
        ch.write(request);
        writeBufferedBodyContent(request, ch);
        ch.flush();
        //Get ready to read origin's response
        ch.read();
        //originConn = conn;
        channelCtx.read();
    }



    private static void writeBufferedBodyContent(final HttpRequestMessage zuulRequest, final Channel channel) {
        zuulRequest.getBodyContents().forEach((chunk) -> {
            channel.write(chunk.retain());
        });
    }


    OriginResponseReceiver getOriginResponseReceiver() {
        return new OriginResponseReceiver(this);
    }

}
