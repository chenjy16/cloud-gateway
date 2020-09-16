package org.cloud.gateway.message;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.netty.filter.GatewayFilter;
import org.cloud.gateway.netty.service.SessionContext;

public interface ZuulMessage extends Cloneable {

    /**
     * Returns the session context of this message.
     */
    SessionContext getContext();

    /**
     * Returns the headers for this message.  They may be request or response headers, depending on the underlying type
     * of this object.  For some messages, there may be no headers, such as with chunked requests or responses.  In this
     * case, a non-{@code null} default headers value will be returned.
     */
    Headers getHeaders();

    /**
     * Sets the headers for this message.
     *
     * @throws NullPointerException if newHeaders is {@code null}.
     */
    void setHeaders(Headers newHeaders);

    /**
     * Returns if this message has an attached body.   For requests, this is typically an HTTP POST body.  For
     * responses, this is typically the HTTP response.
     */
    boolean hasBody();

    /**
     *  Declares that this message has a body.   This method is automatically called when {@link #bufferBodyContents}
     *  is invoked.
     */
    void setHasBody(boolean hasBody);

    /**
     * Returns the message body.  If there is no message body, this returns {@code null}.
     */
    byte[] getBody();

    /**
     * Returns the length of the message body, or {@code 0} if there isn't a message present.
     */
    int getBodyLength();

    /**
     * Sets the message body.  Note: if the {@code body} is {@code null}, this may not reset the body presence as
     * returned by {@link #hasBody}.  The body is considered complete after calling this method.
     */
    void setBody(byte[] body);

    /**
     * Sets the message body as UTF-8 encoded text.   Note that this does NOT set any headers related to the
     * Content-Type; callers must set or reset the content type to UTF-8.  The body is considered complete after
     * calling this method.
     */
    void setBodyAsText( String bodyText);

    /**
     * Appends an HTTP content chunk to this message.  Callers should be careful not to add multiple chunks that
     * implement {@link LastHttpContent}.
     *
     * @throws NullPointerException if {@code chunk} is {@code null}.
     */
    void bufferBodyContents(HttpContent chunk);

    /**
     * Returns the HTTP content chunks that are part of this message.  Callers should avoid retaining the return value,
     * as the contents may change with subsequent body mutations.
     */
    Iterable<HttpContent> getBodyContents();

    /**
     * Sets the message body to be complete if it was not already so.
     *
     * @return {@code true} if the body was not yet complete, or else false.
     */
    boolean finishBufferedBodyIfIncomplete();

    /**
     * Indicates that the message contains a content chunk the implements {@link LastHttpContent}.
     */
    boolean hasCompleteBody();

    /**
     * Passes the body content chunks through the given filter, and sets them back into this message.
     */
    void runBufferedBodyContentThroughFilter(GatewayFilter filter);

    /**
     * Clears the content chunks of this body, calling {@code release()} in the process.  Users SHOULD call this method
     * when the body content is no longer needed.
     */
    void disposeBufferedBody();

    /**
     * Gets the body of this message as UTF-8 text, or {@code null} if there is no body.
     */
    String getBodyAsText();

    /**
     * Returns the maximum body size that this message is willing to hold.  This value value should be more than the
     * sum of lengths of the body chunks.  The max body size may not be strictly enforced, and is informational.
     */
    int getMaxBodySize();

    /**
     * Returns a copy of this message.
     */
    ZuulMessage clone();

    /**
     * Returns a string that reprsents this message which is suitable for debugging.
     */
    String getInfoForLogging();
}

