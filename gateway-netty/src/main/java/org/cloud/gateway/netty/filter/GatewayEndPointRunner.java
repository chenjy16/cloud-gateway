package org.cloud.gateway.netty.filter;

import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.HttpRequestMessage;
import org.cloud.gateway.message.HttpResponseMessage;
import org.cloud.gateway.message.ZuulMessage;


public class GatewayEndPointRunner extends BaseZuulFilterRunner<HttpRequestMessage, HttpResponseMessage>{


    FilterRunner<HttpResponseMessage, HttpResponseMessage> respFilters;


    public GatewayEndPointRunner(FilterRunner<HttpResponseMessage, ? extends ZuulMessage> nextStage) {
        super(nextStage,null);
    }




    public void filter(final HttpRequestMessage zuulReq) {

        // HttpResponseMessage zuulResp = filter(new ProxyEndpoint(zuulReq, zuulReq.getContext().get(NETTY_SERVER_CHANNEL_HANDLER_CONTEXT), respFilters, MethodBinding.NO_OP_BINDING), zuulReq);
    }



    @Override
    public void filter(HttpRequestMessage zuulMesg, HttpContent chunk) {

    }





}
