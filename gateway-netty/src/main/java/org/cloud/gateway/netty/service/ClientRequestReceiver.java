package org.cloud.gateway.netty.service;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

public class ClientRequestReceiver extends ChannelDuplexHandler {


    private HttpRequest clientRequest;

    private final SessionContextDecorator decorator;

    public ClientRequestReceiver(SessionContextDecorator decorator) {
        this.decorator = decorator;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof LastHttpContent) {
            //ctx.channel().attr(ATTR_LAST_CONTENT_RECEIVED).set(Boolean.TRUE);
        }

        if (msg instanceof HttpRequest) {
            clientRequest = (HttpRequest) msg;

            // Handle invalid HTTP requests.
            if (clientRequest.decoderResult().isFailure()) {

            }
        }else if (msg instanceof HttpContent) {


        }else if (msg instanceof HAProxyMessage) {


        }else {

        }

    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {



    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof HttpResponse) {
            promise.addListener((future) -> {
                if (! future.isSuccess()) {

                }
            });
            super.write(ctx, msg, promise);
        }
        else if (msg instanceof HttpContent) {
            promise.addListener((future) -> {
                if (! future.isSuccess())  {

                }
            });
            super.write(ctx, msg, promise);
        }
        else {


        }

    }


}
