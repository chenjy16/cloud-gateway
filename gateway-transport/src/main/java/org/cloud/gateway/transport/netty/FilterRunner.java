package org.cloud.gateway.transport.netty;

import com.netflix.zuul.message.ZuulMessage;

import io.netty.handler.codec.http.HttpContent;

public interface FilterRunner<I extends ZuulMessage, O extends ZuulMessage> {

    void filter(I zuulMesg);
    void filter(I zuulMesg, HttpContent chunk);
}
