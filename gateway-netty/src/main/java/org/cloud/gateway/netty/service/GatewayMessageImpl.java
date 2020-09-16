package org.cloud.gateway.netty.service;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import io.netty.buffer.*;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.LastHttpContent;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;

public class GatewayMessageImpl implements ZuulMessage {


    protected static final DynamicIntProperty MAX_BODY_SIZE_PROP = DynamicPropertyFactory.getInstance().getIntProperty(
            "zuul.message.body.max.size", 25 * 1000 * 1024);
    private static final Charset CS_UTF8 = Charset.forName("UTF-8");

    protected final SessionContext context;
    protected Headers headers;

    private boolean hasBody;
    private boolean bodyBufferedCompletely;
    private List<HttpContent> bodyChunks;


    public GatewayMessageImpl(SessionContext context) {
        this(context, new Headers());
    }

    public GatewayMessageImpl(SessionContext context, Headers headers) {
        this.context = context == null ? new SessionContext() : context;
        this.headers = headers == null ? new Headers() : headers;
        this.bodyChunks = new ArrayList<>(16);
    }

    @Override
    public SessionContext getContext() {
        return context;
    }

    @Override
    public Headers getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Headers newHeaders) {
        this.headers = newHeaders;
    }

    @Override
    public int getMaxBodySize() {
        return MAX_BODY_SIZE_PROP.get();
    }


    @Override
    public void setHasBody(boolean hasBody) {
        this.hasBody = hasBody;
    }

    @Override
    public boolean hasBody() {
        return hasBody;
    }

    @Override
    public boolean hasCompleteBody() {
        return bodyBufferedCompletely;
    }

    @Override
    public void bufferBodyContents(final HttpContent chunk) {
        setHasBody(true);
        bodyChunks.add(chunk);
        if (chunk instanceof  LastHttpContent) {
            bodyBufferedCompletely = true;
        }
    }

    private void setContentLength(int length) {
        headers.remove(HttpHeaderNames.TRANSFER_ENCODING);
        headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(length));
    }

    @Override
    public void setBodyAsText(String bodyText) {
        disposeBufferedBody();
        if (! Strings.isNullOrEmpty(bodyText)) {
            final ByteBuf content = Unpooled.copiedBuffer(bodyText.getBytes(Charsets.UTF_8));
            bufferBodyContents(new DefaultLastHttpContent(content));
            setContentLength(bodyText.getBytes(CS_UTF8).length);
        } else {
            bufferBodyContents(new DefaultLastHttpContent());
            setContentLength(0);
        }
    }

    @Override
    public void setBody(byte[] body) {
        disposeBufferedBody();
        if (body != null && body.length > 0) {
            final ByteBuf content = Unpooled.copiedBuffer(body);
            bufferBodyContents(new DefaultLastHttpContent(content));
            setContentLength(body.length);
        } else {
            bufferBodyContents(new DefaultLastHttpContent());
            setContentLength(0);
        }
    }

    @Override
    public String getBodyAsText() {
        final byte[] body = getBody();
        return (body != null && body.length > 0) ? new String(getBody(), Charsets.UTF_8) : null;
    }

    @Override
    public byte[] getBody() {
        if (bodyChunks.size() == 0) {
            return null;
        }

        int size = 0;
        for (final HttpContent chunk : bodyChunks) {
            size += chunk.content().readableBytes();
        }
        final byte[] body = new byte[size];
        int offset = 0;
        for (final HttpContent chunk : bodyChunks) {
            final ByteBuf content = chunk.content();
            final int len = content.readableBytes();
            content.getBytes(content.readerIndex(), body, offset, len);
            offset += len;
        }
        return body;
    }

    @Override
    public int getBodyLength() {
        int size = 0;
        for (final HttpContent chunk : bodyChunks) {
            size += chunk.content().readableBytes();
        }
        return size;
    }

    @Override
    public Iterable<HttpContent> getBodyContents() {
        return Collections.unmodifiableList(bodyChunks);
    }

    @Override
    public boolean finishBufferedBodyIfIncomplete() {
        if (! bodyBufferedCompletely) {
            bufferBodyContents(new DefaultLastHttpContent());
            return true;
        }
        return false;
    }

    @Override
    public void disposeBufferedBody() {
        bodyChunks.forEach(chunk -> {
            if ((chunk != null) && (chunk.refCnt() > 0)) {
                chunk.release();
            }
        });
        bodyChunks.clear();
    }

    @Override
    public void runBufferedBodyContentThroughFilter(ZuulFilter filter) {
        //Loop optimized for the common case: Most filters' processContentChunk() return
        // original chunk passed in as is without any processing
        for (int i=0; i < bodyChunks.size(); i++) {
            final HttpContent origChunk = bodyChunks.get(i);
            final HttpContent filteredChunk = filter.processContentChunk(this, origChunk);
            if ((filteredChunk != null) && (filteredChunk != origChunk)) {
                //filter actually did some processing, set the new chunk in and release the old chunk.
                bodyChunks.set(i, filteredChunk);
                final int refCnt = origChunk.refCnt();
                if (refCnt > 0) {
                    origChunk.release(refCnt);
                }
            }
        }
    }

    @Override
    public ZuulMessage clone() {
        final GatewayMessageImpl copy = new GatewayMessageImpl(context.clone(), headers.clone());
        this.bodyChunks.forEach(chunk -> {
            chunk.retain();
            copy.bufferBodyContents(chunk);
        });
        return copy;
    }

    /**
     * Override this in more specific subclasses to add request/response info for logging purposes.
     *
     * @return
     */
    @Override
    public String getInfoForLogging()
    {
        return "ZuulMessage";
    }
}
