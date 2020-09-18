package org.cloud.gateway.utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.Http2StreamChannel;
import org.cloud.gateway.message.GatewayMessage;
import org.cloud.gateway.message.Headers;
import org.cloud.gateway.message.HttpRequestInfo;
import org.springframework.util.StringUtils;

public class HttpUtils {

    private static final char[] MALICIOUS_HEADER_CHARS = {'\r', '\n'};

    /**
     * Get the IP address of client making the request.
     *
     * Uses the "x-forwarded-for" HTTP header if available, otherwise uses the remote
     * IP of requester.
     *
     * @param request <code>HttpRequestMessage</code>
     * @return <code>String</code> IP address
     */
    public static String getClientIP(HttpRequestInfo request)
    {
        final String xForwardedFor = request.getHeaders().getFirst(HttpHeaderNames.X_FORWARDED_FOR);
        String clientIP;
        if (xForwardedFor == null) {
            clientIP = request.getClientIp();
        } else {
            clientIP = extractClientIpFromXForwardedFor(xForwardedFor);
        }
        return clientIP;
    }

    /**
     * Extract the client IP address from an x-forwarded-for header. Returns null if there is no x-forwarded-for header
     *
     * @param xForwardedFor a <code>String</code> value
     * @return a <code>String</code> value
     */
    public static String extractClientIpFromXForwardedFor(String xForwardedFor) {
        if (xForwardedFor == null) {
            return null;
        }
        xForwardedFor = xForwardedFor.trim();
        String tokenized[] = xForwardedFor.split(",");
        if (tokenized.length == 0) {
            return null;
        } else {
            return tokenized[0].trim();
        }
    }

    /**
     * return true if the client requested gzip content
     *
     * @param contentEncoding a <code>String</code> value
     * @return true if the content-encoding param containg gzip
     */
    public static boolean isGzipped(String contentEncoding) {
        return contentEncoding.contains("gzip");
    }

    public static boolean isGzipped(Headers headers) {
        String ce = headers.getFirst(HttpHeaderNames.CONTENT_ENCODING);
        return ce != null && isGzipped(ce);
    }

    public static boolean acceptsGzip(Headers headers) {
        String ae = headers.getFirst(HttpHeaderNames.ACCEPT_ENCODING);
        return ae != null && isGzipped(ae);
    }

    /**
     * Ensure decoded new lines are not propagated in headers, in order to prevent XSS
     *
     * @param input - decoded header string
     * @return - clean header string
     */
    public static String stripMaliciousHeaderChars(String input)
    {
        for (char c : MALICIOUS_HEADER_CHARS) {
            input = StringUtils.remove(input, c);
        }
        return input;
    }


    public static boolean hasNonZeroContentLengthHeader(GatewayMessage msg)
    {
        final Integer contentLengthVal = getContentLengthIfPresent(msg);
        return (contentLengthVal != null) && (contentLengthVal.intValue() > 0);
    }

    public static Integer getContentLengthIfPresent(GatewayMessage msg)
    {
        final String contentLengthValue = msg.getHeaders().getFirst(com.netflix.zuul.message.http.HttpHeaderNames.CONTENT_LENGTH);
        if (StringUtils.isNotEmpty(contentLengthValue) && StringUtils.isNumeric(contentLengthValue)) {
            try {
                return Integer.valueOf(contentLengthValue);
            }
            catch (NumberFormatException e) {
                LOG.info("Invalid Content-Length header value on request. " +
                        "value = " + String.valueOf(contentLengthValue));
            }
        }
        return null;
    }

    public static Integer getBodySizeIfKnown(GatewayMessage msg) {
        final Integer bodySize = getContentLengthIfPresent(msg);
        if (bodySize != null) {
            return bodySize.intValue();
        }
        if (msg.hasCompleteBody()) {
            return msg.getBodyLength();
        }
        return null;
    }

    public static boolean hasChunkedTransferEncodingHeader(GatewayMessage msg)
    {
        boolean isChunked = false;
        String teValue = msg.getHeaders().getFirst(com.netflix.zuul.message.http.HttpHeaderNames.TRANSFER_ENCODING);
        if (StringUtils.isNotEmpty(teValue)) {
            isChunked = "chunked".equals(teValue.toLowerCase());
        }
        return isChunked;
    }

    /**
     * If http/1 then will always want to just use ChannelHandlerContext.channel(), but for http/2
     * will want the parent channel (as the child channel is different for each h2 stream).
     */
    public static Channel getMainChannel(ChannelHandlerContext ctx)
    {
        return getMainChannel(ctx.channel());
    }

    public static Channel getMainChannel(Channel channel)
    {
        if (channel instanceof Http2StreamChannel) {
            return channel.parent();
        }
        return channel;
    }
}

