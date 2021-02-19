package org.cloud.gateway.transport.webflux.plugin.before;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;


public class GwServerWebExchangeDecorator extends ServerWebExchangeDecorator{

    private GwServerHttpRequestDecorator requestDecorator;
    private GwServerHttpResponseDecorator responseDecorator;


    public GwServerWebExchangeDecorator(ServerWebExchange delegate) {
        super(delegate);
        requestDecorator=new GwServerHttpRequestDecorator(delegate.getRequest());
        responseDecorator=new GwServerHttpResponseDecorator(delegate.getResponse());
    }

    public GwServerHttpRequestDecorator getRequestDecorator() {
        return requestDecorator;
    }

    public GwServerHttpResponseDecorator getResponseDecorator() {
        return responseDecorator;
    }
}
