package org.cloud.gateway.netty.service;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import java.util.Map;

public class ClientRequestReceiver extends ChannelDuplexHandler {


    private HttpRequest clientRequest;

    private HttpRequestMessage zuulRequest;

    private final SessionContextDecorator decorator;

    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";

    public static final AttributeKey<HttpRequestMessage> ATTR_ZUUL_REQ = AttributeKey.newInstance("_zuul_request");

    public static final AttributeKey<Boolean> ATTR_LAST_CONTENT_RECEIVED = AttributeKey.newInstance("_last_content_received");

    public ClientRequestReceiver(SessionContextDecorator decorator) {
        this.decorator = decorator;
    }



    /**
     * @desc   
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
            zuulRequest = buildZuulHttpRequest(clientRequest, ctx);
            handleExpect100Continue(ctx, clientRequest);


            // Handle invalid HTTP requests.
            if (clientRequest.decoderResult().isFailure()) {





            }
        }else if (msg instanceof HttpContent) {




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
                   // throw new ZuulException( s.cause(), "Failed while writing 100-continue response", true);
                }
            });
            // Remove the Expect: 100-Continue header from request as we don't want to proxy it downstream.
            req.headers().remove(HttpHeaderNames.EXPECT);
            //zuulRequest.getHeaders().remove(HttpHeaderNames.EXPECT.toString());
        }
    }



    private HttpRequestMessage buildZuulHttpRequest(final HttpRequest nativeRequest, final ChannelHandlerContext clientCtx) {
        // Setup the context for this request.
        final SessionContext context;
        if (decorator != null) { // Optionally decorate the context.
            SessionContext tempContext = new SessionContext();
            // Store the netty channel in SessionContext.
            tempContext.set("", clientCtx);
            context = decorator.decorate(tempContext);
        }
        else {
            context = new SessionContext();
        }

        // Get the client IP (ignore XFF headers at this point, as that can be app specific).
        final Channel channel = clientCtx.channel();
        final String clientIp = (String)channel.attr(AttributeKey.newInstance("_source_address")).get();

        // This is the only way I found to get the port of the request with netty...
        final int port =(int) channel.attr(AttributeKey.newInstance("_server_local_port")).get();
        final String serverName = (String)channel.attr(AttributeKey.newInstance("_server_local_address")).get();



        String scheme = SCHEME_HTTP;


        // Decide if this is HTTP/1 or HTTP/2.
        String protocol = (String)channel.attr(AttributeKey.valueOf("protocol_name")).get();
        if (protocol == null) {
            protocol = nativeRequest.protocolVersion().text();
        }

        // Strip off the query from the path.
        String path = nativeRequest.uri();
        int queryIndex = path.indexOf('?');
        if (queryIndex > -1) {
            path = path.substring(0, queryIndex);
        }

        // Setup the req/resp message objects.
        final HttpRequestMessage request = new HttpRequestMessageImpl(
                context,
                protocol,
                nativeRequest.method().asciiName().toString().toLowerCase(),
                path,
                copyQueryParams(nativeRequest),
                copyHeaders(nativeRequest),
                clientIp,
                scheme,
                port,
                serverName
        );

        // Try to decide if this request has a body or not based on the headers (as we won't yet have
        // received any of the content).
        // NOTE that we also later may override this if it is Chunked encoding, but we receive
        // a LastHttpContent without any prior HttpContent's.
      /*  if (HttpUtils.hasChunkedTransferEncodingHeader(request) || HttpUtils.hasNonZeroContentLengthHeader(request)) {
            request.setHasBody(true);
        }

        // Store this original request info for future reference (ie. for metrics and access logging purposes).
        request.storeInboundRequest();

        // Store the netty request for use later.
        context.set(CommonContextKeys.NETTY_HTTP_REQUEST, nativeRequest);

        // Store zuul request on netty channel for later use.
        channel.attr(ATTR_ZUUL_REQ).set(request);

        if (nativeRequest instanceof DefaultFullHttpRequest) {
            final ByteBuf chunk = ((DefaultFullHttpRequest) nativeRequest).content();
            request.bufferBodyContents(new DefaultLastHttpContent(chunk));
        }*/

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
