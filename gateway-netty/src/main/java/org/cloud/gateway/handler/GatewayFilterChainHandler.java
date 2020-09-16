package org.cloud.gateway.handler;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.unix.Errors;
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.ReferenceCountUtil;
import org.cloud.gateway.message.HttpRequestMessage;
import org.cloud.gateway.message.HttpResponseMessage;
import org.cloud.gateway.netty.filter.GatewayFilterChainRunner;

import java.nio.channels.ClosedChannelException;



public class GatewayFilterChainHandler extends ChannelInboundHandlerAdapter {

    private HttpRequestMessage gatewayRequest;
    private GatewayFilterChainRunner<HttpRequestMessage> requestFilterChain;
    private GatewayFilterChainRunner<HttpResponseMessage> responseFilterChain;



    public GatewayFilterChainHandler(GatewayFilterChainRunner<HttpRequestMessage> requestFilterChain,GatewayFilterChainRunner<HttpResponseMessage> responseFilterChain) {

        this.requestFilterChain = Preconditions.checkNotNull(requestFilterChain, "request filter chain");
        this.responseFilterChain = Preconditions.checkNotNull(responseFilterChain, "response filter chain");
    }



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequestMessage) {//http header 部分
            gatewayRequest = (HttpRequestMessage)msg;
            requestFilterChain.filter(gatewayRequest);
        } else if ((msg instanceof HttpContent)&&(gatewayRequest != null)) {//http body 部分，如果有多个，那最后一个就是LastHttpContent
            requestFilterChain.filter(gatewayRequest, (HttpContent) msg);
        }else {
            ReferenceCountUtil.release(msg);
        }
    }



    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }



    protected HttpRequestMessage getZuulRequest() {
        return gatewayRequest;
    }



    protected void fireEndpointFinish(final boolean error) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {


    }


    // Race condition: channel.isActive() did not catch
    // channel close..resulting in an i/o exception
    private boolean isClientChannelClosed(Throwable cause) {
        if (cause instanceof ClosedChannelException ||
                cause instanceof Errors.NativeIoException) {

            return true;
        }
        return false;
    }



}
