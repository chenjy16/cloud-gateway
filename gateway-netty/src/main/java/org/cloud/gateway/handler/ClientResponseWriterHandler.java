package org.cloud.gateway.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.cloud.gateway.message.Header;
import org.cloud.gateway.message.HttpRequestInfo;
import org.cloud.gateway.message.HttpRequestMessage;
import org.cloud.gateway.message.HttpResponseMessage;
import org.cloud.gateway.netty.service.HttpLifecycleChannelHandler;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import java.rmi.registry.Registry;

public class ClientResponseWriterHandler extends ChannelInboundHandlerAdapter {



    private final RequestCompleteHandler requestCompleteHandler;
    //state
    private boolean isHandlingRequest;
    private boolean startedSendingResponseToClient;
    private boolean closeConnection;
    public static final AttributeKey<HttpResponseMessage> ATTR_ZUUL_RESP = AttributeKey.newInstance("_zuul_response");
    //data
    private HttpResponseMessage hrm;
    public static final String NETTY_HTTP_REQUEST = "_netty_http_request";


    public ClientResponseWriterHandler(RequestCompleteHandler requestCompleteHandler) {
        this(requestCompleteHandler, null);
    }

    public ClientResponseWriterHandler(RequestCompleteHandler requestCompleteHandler, Registry registry) {
        this.requestCompleteHandler = requestCompleteHandler;
    }




    /**
     * @desc
     * @author chenjianyu944@gmail.com
     * @date   2020/9/17 17:25
     **/
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        final Channel channel = ctx.channel();
        if (msg instanceof HttpResponseMessage) {
            final HttpResponseMessage resp = (HttpResponseMessage) msg;

            if (skipProcessing(resp)) {
                return;
            }

            if ((! isHandlingRequest) || (startedSendingResponseToClient)) {
                /* This can happen if we are already in the process of streaming response back to client OR NOT within active
                   request/response cycle and something like IDLE or Request Read timeout occurs. In that case we have no way
                   to recover other than closing the socket and cleaning up resources used by BOTH responses.
                 */
                resp.disposeBufferedBody();
                if (hrm != null) hrm.disposeBufferedBody();
                ctx.close(); //This will trigger CompleteEvent if one is needed
                return;
            }

            startedSendingResponseToClient = true;
            hrm = resp;
            if ("close".equalsIgnoreCase(hrm.getHeaders().getFirst("Connection"))) {
                closeConnection = true;
            }
            channel.attr(ATTR_ZUUL_RESP).set(hrm);

            if (channel.isActive()) {

                // Track if this is happening.
              /*  if (! ClientRequestReceiverHandler.isLastContentReceivedForChannel(channel)) {

                    StatusCategory status = StatusCategoryUtils.getStatusCategory(ClientRequestReceiver.getRequestFromChannel(channel));
                    if (ZuulStatusCategory.FAILURE_CLIENT_TIMEOUT.equals(status)) {
                        // If the request timed-out while being read, then there won't have been any LastContent, but thats ok because the connection will have to be discarded anyway.
                    }
                    else {
                        responseBeforeReceivedLastContentCounter.increment();
                        LOG.warn("Writing response to client channel before have received the LastContent of request! "
                                + zuulResponse.getInboundRequest().getInfoForLogging() + ", "
                                + ChannelUtils.channelInfoForLogging(channel));
                    }
                }*/

                // Write out and flush the response to the client channel.
                channel.write(buildHttpResponse(hrm));
                writeBufferedBodyContent(hrm, channel);
                channel.flush();
            } else {
                channel.close();
            }
        }
        else if (msg instanceof HttpContent) {
            final HttpContent chunk = (HttpContent) msg;
            if (channel.isActive()) {
                channel.writeAndFlush(chunk);
            } else {
                chunk.release();
                channel.close();
            }
        }
        else {
            //should never happen
            ReferenceCountUtil.release(msg);
            //throw new ZuulException("Received invalid message from origin", true);
        }
    }




    protected boolean skipProcessing(HttpResponseMessage resp) {
        // override if you need to skip processing of response
        return false;
    }

    private static void writeBufferedBodyContent(final HttpResponseMessage zuulResponse, final Channel channel) {
        zuulResponse.getBodyContents().forEach(chunk -> channel.write(chunk.retain()));
    }




    /**
     * @desc
     * @author chenjianyu944@gmail.com
     * @date   2020/9/17 17:28
     **/
    private HttpResponse buildHttpResponse(final HttpResponseMessage zuulResp) {
        final HttpRequestInfo zuulRequest = zuulResp.getInboundRequest();
        HttpVersion responseHttpVersion;
        final String inboundProtocol = zuulRequest.getProtocol();
        if (inboundProtocol.startsWith("HTTP/1")) {
            responseHttpVersion = HttpVersion.valueOf(inboundProtocol);
        }
        else {
            // Default to 1.1. We do this to cope with HTTP/2 inbound requests.
            responseHttpVersion = HttpVersion.HTTP_1_1;
        }

        // Create the main http response to send, with body.
        final DefaultHttpResponse nativeResponse = new DefaultHttpResponse(responseHttpVersion,
                HttpResponseStatus.valueOf(zuulResp.getStatus()), false, false);

        // Now set all of the response headers - note this is a multi-set in keeping with HTTP semantics
        final HttpHeaders nativeHeaders = nativeResponse.headers();
        for (Header entry : zuulResp.getHeaders().entries()) {
            nativeHeaders.add(entry.getKey(), entry.getValue());
        }

        // Netty does not automatically add Content-Length or Transfer-Encoding: chunked. So we add here if missing.
        if (! HttpUtil.isContentLengthSet(nativeResponse) && ! HttpUtil.isTransferEncodingChunked(nativeResponse)) {
            nativeResponse.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        }

        final HttpRequest nativeReq = (HttpRequest) zuulResp.getContext().get(NETTY_HTTP_REQUEST);
        if (!closeConnection && HttpUtil.isKeepAlive(nativeReq)) {
            HttpUtil.setKeepAlive(nativeResponse, true);
        } else {
            // Send a Connection: close response header (only needed for HTTP/1.0 but no harm in doing for 1.1 too).
            nativeResponse.headers().set("Connection", "close");
        }

        return nativeResponse;
    }







    /**
     * @desc
     * @author chenjianyu944@gmail.com
     * @date   2020/9/17 17:26
     **/
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof HttpLifecycleChannelHandler.StartEvent) {
            isHandlingRequest = true;
            startedSendingResponseToClient = false;
            closeConnection = false;
            hrm = null;
        }else if (evt instanceof HttpLifecycleChannelHandler.CompleteEvent) {
            HttpResponse response = ((HttpLifecycleChannelHandler.CompleteEvent) evt).getResponse();
            if (response != null) {
                if ("close".equalsIgnoreCase(response.headers().get("Connection"))) {
                    closeConnection = true;
                }
            }
            if (hrm != null) {
                hrm.disposeBufferedBody();
            }

            // Do all the post-completion metrics and logging.
            handleComplete(ctx.channel());

            // Choose to either close the connection, or prepare it for next use.
            final HttpLifecycleChannelHandler.CompleteEvent completeEvent = (HttpLifecycleChannelHandler.CompleteEvent)evt;
            final HttpLifecycleChannelHandler.CompleteReason reason = completeEvent.getReason();
            if (reason == HttpLifecycleChannelHandler.CompleteReason.SESSION_COMPLETE || reason == HttpLifecycleChannelHandler.CompleteReason.INACTIVE) {
                if (! closeConnection) {
                    //Start reading next request over HTTP 1.1 persistent connection
                    ctx.channel().read();
                } else {
                    ctx.close();
                }
            }
            else {
               /* if (isHandlingRequest) {
                    LOG.warn("Received complete event while still handling the request. With reason: " + reason.name() + " -- " +
                            ChannelUtils.channelInfoForLogging(ctx.channel()));
                }*/
                ctx.close();
            }

            isHandlingRequest = false;
        } else if (evt instanceof IdleStateEvent) {
            //LOG.debug("Received IdleStateEvent.");
        } else {
            //LOG.info("ClientResponseWriter Received event {}", evt);
        }
    }


    private void handleComplete(Channel channel) {
        try {
            if ((isHandlingRequest)) {
                // Notify requestComplete listener if configured.
                final HttpRequestMessage zuulRequest = ClientRequestReceiverHandler.getRequestFromChannel(channel);
                if ((requestCompleteHandler != null) && (zuulRequest != null)) {
                    requestCompleteHandler.handle(zuulRequest.getInboundRequest(), hrm);
                }
            }
        }
        catch (Throwable ex) {
            //LOG.error("Error in RequestCompleteHandler.", ex);
        }
    }

    protected void completeMetrics(Channel channel, HttpResponseMessage zuulResponse) {
        // override for recording complete metrics
    }






    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        int status = 500;
        //final String errorMsg = "ClientResponseWriter caught exception in client connection pipeline: " + ChannelUtils.channelInfoForLogging(ctx.channel());

     /*   if (cause instanceof ZuulException) {
            final ZuulException ze = (ZuulException) cause;
            status = ze.getStatusCode();
            LOG.error(errorMsg, cause);
        } else if (cause instanceof ReadTimeoutException) {
            LOG.error(errorMsg + ", Read timeout fired");
            status = 504;
        }else {
            LOG.error(errorMsg, cause);
        }*/
        if (isHandlingRequest && !startedSendingResponseToClient && ctx.channel().isActive()) {
            final HttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(status));
            ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
            startedSendingResponseToClient = true;
        }else {
            ctx.close();
        }
    }



    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ctx.close();
    }

}

