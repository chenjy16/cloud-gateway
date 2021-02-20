package org.cloud.gateway.transport.webflux.utils;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;


public class ServerWebExchangeUtils {


    public static <T>  Mono<T> cacheRequestBody(ServerWebExchange exchange,Function<ServerHttpRequest,Mono<T>> function){
        return DataBufferUtils.join(exchange.getRequest().getBody()).map(dataBuffer->{
            if(dataBuffer.readableByteCount()>0){
                exchange.getAttributes().put("Cached_req_body_attr",dataBuffer);
            }
            ServerHttpRequest decorator=new ServerHttpRequestDecorator(exchange.getRequest()){

                @Override
                public Flux<DataBuffer>  getBody(){
                    return Mono.<DataBuffer>fromSupplier(()->{
                        if(Objects.isNull(exchange.getAttributeOrDefault("Cached_req_body_attr",null))){
                            return null;
                        }
                        NettyDataBuffer  pdb=(NettyDataBuffer)dataBuffer;
                        return pdb.factory().wrap(pdb.getNativeBuffer().retainedSlice());
                    }).flux();
                }

            };
            return decorator;
        }).switchIfEmpty(Mono.just(exchange.getRequest())).flatMap(function);


    }
}



