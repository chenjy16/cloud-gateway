package org.cloud.gateway.netty.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpLifecycleChannelHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HttpLifecycleChannelHandler.class);

    public static final AttributeKey<HttpRequest> ATTR_HTTP_REQ = AttributeKey.newInstance("_http_request");
    public static final AttributeKey<HttpResponse> ATTR_HTTP_RESP = AttributeKey.newInstance("_http_response");

    protected enum State {
        STARTED, COMPLETED
    }

    private static final AttributeKey<State> ATTR_STATE = AttributeKey.newInstance("_httplifecycle_state");

    
    /**
     * @desc   设置开始事件
     * @author chenjianyu944@gmail.com
     * @date   2020/6/21 17:26
     **/
    protected static boolean fireStartEvent(ChannelHandlerContext ctx, HttpRequest request) {
        // Only allow this method to run once per request.
        Channel channel = ctx.channel();
        Attribute<State> attr = channel.attr(ATTR_STATE);
        State state = attr.get();

        if (state == State.STARTED) {
            // This could potentially happen if a bad client sends a 2nd request on the same connection
            // without waiting for the response from the first. And we don't support HTTP Pipelining.
            LOG.error("Received a http request on connection where we already have a request being processed. " +
                    "Closing the connection now. channel = " + channel.id().asLongText());
            channel.close();
            ctx.pipeline().fireUserEventTriggered(new RejectedPipeliningEvent());
            return false;
        }

        channel.attr(ATTR_STATE).set(State.STARTED);
        channel.attr(ATTR_HTTP_REQ).set(request);
        ctx.pipeline().fireUserEventTriggered(new StartEvent(request));

        return true;
    }

    
    /**
     * @desc   设置完成事件
     * @author chenjianyu944@gmail.com
     * @date   2020/6/21 17:27
     **/
    protected static boolean fireCompleteEventIfNotAlready(ChannelHandlerContext ctx, CompleteReason reason) {
        // Only allow this method to run once per request.
        Attribute<State> attr = ctx.channel().attr(ATTR_STATE);
        State state = attr.get();

        if (state == null || state != State.STARTED)
            return false;

        attr.set(State.COMPLETED);
        HttpRequest request = ctx.channel().attr(ATTR_HTTP_REQ).get();
        HttpResponse response = ctx.channel().attr(ATTR_HTTP_RESP).get();

        // Cleanup channel attributes.
        ctx.channel().attr(ATTR_HTTP_REQ).set(null);
        ctx.channel().attr(ATTR_HTTP_RESP).set(null);

        // Fire the event to whole pipeline.
        ctx.pipeline().fireUserEventTriggered(new CompleteEvent(reason, request, response));

        return true;
    }






    public enum CompleteReason {
        SESSION_COMPLETE,
        INACTIVE,
        DISCONNECT,
        CLOSE
    }

    public static class StartEvent {
        private final HttpRequest request;

        public StartEvent(HttpRequest request){
            this.request = request;
        }

        public HttpRequest getRequest() {
            return request;
        }
    }

    public static class CompleteEvent {
        private final CompleteReason reason;
        private final HttpRequest request;
        private final HttpResponse response;

        public CompleteEvent(CompleteReason reason, HttpRequest request, HttpResponse response) {
            this.reason = reason;
            this.request = request;
            this.response = response;
        }

        public CompleteReason getReason() {
            return reason;
        }

        public HttpRequest getRequest() {
            return request;
        }

        public HttpResponse getResponse() {
            return response;
        }
    }

    public static class RejectedPipeliningEvent
    {}

}
