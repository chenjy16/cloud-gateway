package org.cloud.gateway.handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.cloud.gateway.message.Headers;
import org.cloud.gateway.message.HttpRequestMessage;
import org.cloud.gateway.message.HttpRequestMessageImpl;
import org.cloud.gateway.message.HttpResponseMessage;
import org.cloud.gateway.netty.service.HttpLifecycleChannelHandler;
import org.cloud.gateway.netty.service.HttpQueryParams;
import org.cloud.gateway.netty.service.SessionContext;
import org.cloud.gateway.netty.service.SessionContextDecorator;
import org.cloud.gateway.utils.HttpUtils;

import java.util.Map;




public class ClientRequestReceiverHandler extends ChannelDuplexHandler {
    
    private HttpRequest clientRequest;
    private HttpRequestMessage hrmsg;

    private final SessionContextDecorator decorator;
    public static final String NETTY_HTTP_REQUEST = "_netty_http_request";
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    public static final String NETTY_SERVER_CHANNEL_HANDLER_CONTEXT = "_netty_server_channel_handler_context";
    public static final AttributeKey<HttpRequestMessage> ATTR_ZUUL_REQ = AttributeKey.newInstance("_zuul_request");

    public static final AttributeKey<Boolean> ATTR_LAST_CONTENT_RECEIVED = AttributeKey.newInstance("_last_content_received");
    public static final AttributeKey<HttpResponseMessage> ATTR_ZUUL_RESP = AttributeKey.newInstance("_zuul_response");
    public ClientRequestReceiverHandler(SessionContextDecorator decorator) {
        this.decorator = decorator;
    }



    /**
     * @desc   读取http请求
     * @author chenjianyu944@gmail.com
     * @date   2020/6/28 13:38
     **/
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof LastHttpContent) {
            ctx.channel().attr(ATTR_LAST_CONTENT_RECEIVED).set(Boolean.TRUE);
        }
        if (msg instanceof HttpRequest) {
            clientRequest = (HttpRequest) msg;
            hrmsg = buildGatewayHttpRequest(clientRequest, ctx);
            handleExpect100Continue(ctx, clientRequest);
            // Handle invalid HTTP requests.
            if (clientRequest.decoderResult().isFailure()) {
                //hrmsg.getContext().setError(ze);
                //hrmsg.getContext().setShouldSendErrorResponse(true);
            }else if (hrmsg.hasBody() && hrmsg.getBodyLength() > hrmsg.getMaxBodySize()) {
                //hrmsg.getContext().setError(ze);
                //hrmsg.getContext().setShouldSendErrorResponse(true);
            }
            //Send the request down the filter pipeline
            ctx.fireChannelRead(hrmsg);
        }else if (msg instanceof HttpContent) {
            if ((hrmsg != null) && (! hrmsg.getContext().isCancelled())) {
                ctx.fireChannelRead(msg);
            } else {
                //We already sent response for this request, these are laggard request body chunks that are still arriving
                ReferenceCountUtil.release(msg);
            }
        }else if (msg instanceof HAProxyMessage) {
            ReferenceCountUtil.release(msg);

        }else {
            ReferenceCountUtil.release(msg);
        }

    }


    /**
     * @desc   客户端有一个较大的文件需要上传并保存，但是客户端不知道服务器是否愿意接受这个文件，
     * 所以希望在消耗网络资源进行传输之前，先询问一下服务器的意愿。实际操作为客户端发送一条特殊的请求报文，报文的头部应包含
     * @author chenjianyu944@gmail.com
     * @date   2020/6/21 20:17
     **/
    private void handleExpect100Continue(ChannelHandlerContext ctx, HttpRequest req) {
        if (HttpUtil.is100ContinueExpected(req)) {
            final ChannelFuture f = ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            f.addListener((s) -> {
                if (! s.isSuccess()) {
                    //throw new ZuulException( s.cause(), "Failed while writing 100-continue response", true);
                }
            });
            // Remove the Expect: 100-Continue header from request as we don't want to proxy it downstream.
            req.headers().remove(HttpHeaderNames.EXPECT);
            hrmsg.getHeaders().remove(HttpHeaderNames.EXPECT.toString());
        }
    }




    /**
     * @desc   构建网关内部消息对象
     * @author chenjianyu944@gmail.com
     * @date   2020/9/17 11:49
     **/
    private HttpRequestMessage buildGatewayHttpRequest(final HttpRequest nativeRequest, final ChannelHandlerContext clientCtx) {
        final SessionContext context;
        if (decorator != null) {
            SessionContext tempContext = new SessionContext();
            tempContext.set(NETTY_SERVER_CHANNEL_HANDLER_CONTEXT, clientCtx);
            context = decorator.decorate(tempContext);
        }else {
            context = new SessionContext();
        }


        final Channel channel = clientCtx.channel();
        final String clientIp = (String)channel.attr(AttributeKey.newInstance("_source_address")).get();
        final int port =(int) channel.attr(AttributeKey.newInstance("_server_local_port")).get();
        final String serverName = (String)channel.attr(AttributeKey.newInstance("_server_local_address")).get();


        String scheme = SCHEME_HTTP;
        String protocol = (String)channel.attr(AttributeKey.valueOf("protocol_name")).get();
        if (protocol == null) {
            protocol = nativeRequest.protocolVersion().text();
        }
        String path = nativeRequest.uri();
        int queryIndex = path.indexOf('?');
        if (queryIndex > -1) {
            path = path.substring(0, queryIndex);
        }


        final HttpRequestMessage request = new HttpRequestMessageImpl(context, protocol, nativeRequest.method().asciiName().toString().toLowerCase(),path,copyQueryParams(nativeRequest), copyHeaders(nativeRequest),
                clientIp,
                scheme,
                port,
                serverName
        );


        if (HttpUtils.hasChunkedTransferEncodingHeader(request) || HttpUtils.hasNonZeroContentLengthHeader(request)) {
            request.setHasBody(true);
        }
        request.storeInboundRequest();

        // Store the netty request for use later.
        context.set(NETTY_HTTP_REQUEST, nativeRequest);
        channel.attr(ATTR_ZUUL_REQ).set(request);
        if (nativeRequest instanceof DefaultFullHttpRequest) {
            final ByteBuf chunk = ((DefaultFullHttpRequest) nativeRequest).content();
            request.bufferBodyContents(new DefaultLastHttpContent(chunk));
        }
        return request;
    }





    public static HttpQueryParams copyQueryParams(final HttpRequest nativeRequest) {
        final String uri = nativeRequest.uri();
        int queryStart = uri.indexOf('?');
        final String query = queryStart == -1 ? null : uri.substring(queryStart + 1);
        return HttpQueryParams.parse(query);
    }

    private static Headers copyHeaders(final HttpRequest req) {
        final Headers headers = new Headers();
        for (Map.Entry<String, String> entry : req.headers().entries()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return headers;
    }






    /**
     * @desc
     * @author chenjianyu944@gmail.com
     * @date   2020/9/17 16:52
     **/
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof HttpLifecycleChannelHandler.CompleteEvent) {
            final HttpLifecycleChannelHandler.CompleteReason reason = ((HttpLifecycleChannelHandler.CompleteEvent) evt).getReason();
            if (hrmsg != null) {
                hrmsg.getContext().cancel();
                hrmsg.disposeBufferedBody();
                //final CurrentPassport passport = CurrentPassport.fromSessionContext(zuulRequest.getContext());
               /* if ((passport != null) && (passport.findState(PassportState.OUT_RESP_LAST_CONTENT_SENT) == null)) {
                    // Only log this state if the response does not seem to have completed normally.
                    passport.add(PassportState.IN_REQ_CANCELLED);
                }*/
            }

            if (reason == HttpLifecycleChannelHandler.CompleteReason.INACTIVE) {
                // Client closed connection prematurely.
                //hrmsg.getContext().set("status_category", statusCategory);
            }

            clientRequest = null;
            hrmsg = null;
        }

        super.userEventTriggered(ctx, evt);

        if (evt instanceof HttpLifecycleChannelHandler.CompleteEvent) {
            final Channel channel = ctx.channel();
            channel.attr(ATTR_ZUUL_REQ).set(null);
            channel.attr(ATTR_ZUUL_RESP).set(null);
            channel.attr(ATTR_LAST_CONTENT_RECEIVED).set(null);
        }


    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof HttpResponse) {
            promise.addListener((future) -> {
                if (! future.isSuccess()) {
                    //fireWriteError("response headers", future.cause(), ctx);
                }
            });
            super.write(ctx, msg, promise);
        }
        else if (msg instanceof HttpContent) {
            promise.addListener((future) -> {
                if (! future.isSuccess())  {
                    //fireWriteError("response content", future.cause(), ctx);
                }
            });
            super.write(ctx, msg, promise);
        }
        else {
            //should never happen
            ReferenceCountUtil.release(msg);
            //throw new ZuulException("Attempt to write invalid content type to client: "+msg.getClass().getSimpleName(), true);
        }
    }

    public static boolean isLastContentReceivedForChannel(Channel ch) {
        Boolean value = ch.attr(ATTR_LAST_CONTENT_RECEIVED).get();
        return value == null ? false : value.booleanValue();
    }


    public static HttpRequestMessage getRequestFromChannel(Channel ch) {
        return ch.attr(ATTR_ZUUL_REQ).get();
    }
}
