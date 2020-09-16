package org.cloud.gateway.netty.filter;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.GatewayMessage;

public interface FilterRunner<I extends GatewayMessage, O extends GatewayMessage> {
    void filter(I zuulMesg);
    void filter(I zuulMesg, HttpContent chunk);
}
