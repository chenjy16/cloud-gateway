package org.cloud.gateway.handler;
import org.cloud.gateway.message.HttpRequestInfo;
import org.cloud.gateway.message.HttpResponseMessage;

public interface RequestCompleteHandler {
    void handle(HttpRequestInfo inboundRequest, HttpResponseMessage response);
}
