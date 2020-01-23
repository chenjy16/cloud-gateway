package org.cloud.gateway.transport.netty;

import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.ClientAuth;
import io.netty.util.AsciiString;

import java.util.Collection;

/**
 * Created by cjy on 2020/1/5.
 */
@ChannelHandler.Sharable
public class StripUntrustedProxyHeadersHandler extends ChannelInboundHandlerAdapter
{
    public enum AllowWhen {
        ALWAYS,
        MUTUAL_SSL_AUTH,
        NEVER
    }

    private static final Collection<AsciiString> HEADERS_TO_STRIP = Sets.newHashSet(
            new AsciiString("x-forwarded-for"),
            new AsciiString("x-forwarded-port"),
            new AsciiString("x-forwarded-proto"),
            new AsciiString("x-forwarded-proto-version"),
            new AsciiString("x-real-ip")
    );

    private final AllowWhen allowWhen;

    public StripUntrustedProxyHeadersHandler(AllowWhen allowWhen)
    {
        this.allowWhen = allowWhen;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            switch (allowWhen) {
                case NEVER:
                    stripXFFHeaders(req);
                    break;
               /* case MUTUAL_SSL_AUTH:
                    if (! connectionIsUsingMutualSSLWithAuthEnforced(ctx.channel())) {
                        stripXFFHeaders(req);
                    }
                    break;*/
                case ALWAYS:
                    // Do nothing.
                    break;
                default:
                    // default to not allow.
                    stripXFFHeaders(req);
            }
        }

        super.channelRead(ctx, msg);
    }



    private void stripXFFHeaders(HttpRequest req)
    {
        HttpHeaders headers = req.headers();
        for (AsciiString headerName : HEADERS_TO_STRIP) {
            headers.remove(headerName);
        }
    }
}

