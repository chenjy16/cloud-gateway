package org.cloud.gateway.netty.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.GatewayMessage;
import org.cloud.gateway.message.HttpRequestMessage;
import org.cloud.gateway.message.HttpResponseMessage;



public class GatewayEndPointRunner extends BaseGatewayFilterRunner<HttpRequestMessage, HttpResponseMessage> {

    public static final String NETTY_SERVER_CHANNEL_HANDLER_CONTEXT = "_netty_server_channel_handler_context";
    FilterRunner<HttpResponseMessage, HttpResponseMessage> respFilters;


    public GatewayEndPointRunner(FilterRunner<HttpResponseMessage, ? extends GatewayMessage> nextStage) {
        super(nextStage,null);
    }




    public void filter(final HttpRequestMessage zuulReq) {

         HttpResponseMessage zuulResp = filter(new ProxyEndpoint(zuulReq, (ChannelHandlerContext) zuulReq.getContext().get(NETTY_SERVER_CHANNEL_HANDLER_CONTEXT), respFilters, MethodBinding.NO_OP_BINDING), zuulReq);


    }



    @Override
    public void filter(HttpRequestMessage zuulMesg, HttpContent chunk) {

    }





}
