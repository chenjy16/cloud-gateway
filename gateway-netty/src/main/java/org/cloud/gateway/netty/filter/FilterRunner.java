package org.cloud.gateway.netty.filter;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.ZuulMessage;

public interface FilterRunner<I extends ZuulMessage, O extends ZuulMessage> {
    void filter(I zuulMesg);
    void filter(I zuulMesg, HttpContent chunk);
}
