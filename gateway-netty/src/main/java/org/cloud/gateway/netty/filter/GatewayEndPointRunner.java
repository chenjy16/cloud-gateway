package org.cloud.gateway.netty.filter;

import com.google.common.base.Preconditions;
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



    /**
     * @desc
     * @author chenjianyu944@gmail.com
     * @date   2020/9/16 19:55
     **/
    public void filter(final HttpRequestMessage gatewayReq) {

        if (gatewayReq.getContext().isCancelled()) {
            gatewayReq.disposeBufferedBody();
            return;
        }
        Preconditions.checkNotNull(gatewayReq, "input message");

        ProxyEndpoint endpoint=new ProxyEndpoint(gatewayReq, (ChannelHandlerContext) gatewayReq.getContext().get(NETTY_SERVER_CHANNEL_HANDLER_CONTEXT), respFilters, MethodBinding.NO_OP_BINDING);
        //发送请求到后端
        HttpResponseMessage zuulResp = filter(endpoint, gatewayReq);
        //response过滤器
        if ((zuulResp != null)&&(! (endpoint instanceof ProxyEndpoint))) {
            invokeNextStage(zuulResp);
        }
    }





    @Override
    public void filter(HttpRequestMessage zuulMesg, HttpContent chunk) {

    }





}
