package org.cloud.gateway.webflux.plugin.before;

import com.google.common.io.ByteStreams;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.io.InputStream;

import static reactor.core.scheduler.Schedulers.single;

/**
 * Created by cjy on 2020/1/22.
 */
public class GwServerHttpResponseDecorator extends ServerHttpResponseDecorator{


    public GwServerHttpResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }


    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return super.writeAndFlushWith(body);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

        MediaType contentType=super.getHeaders().getContentType();
        if(body instanceof Mono){
            Mono<DataBuffer> monoBody=(Mono<DataBuffer>)body;
            return super.writeWith(monoBody.publishOn(single()).map(dataBuffer -> handleStream(dataBuffer)));
        }else if(body instanceof Flux){
            Flux<DataBuffer> fluxBody=(Flux<DataBuffer>)body;
            return super.writeWith(fluxBody.publishOn(single()).map(dataBuffer -> handleStream(dataBuffer)));
        }
        return super.writeWith(body);
    }


    public DataBuffer handleStream(DataBuffer buffer){

        try {
            InputStream is=buffer.asInputStream();
            byte[] bytes= ByteStreams.toByteArray(is);
            NettyDataBufferFactory nettyDataBufferFactory=new NettyDataBufferFactory(new UnpooledByteBufAllocator(false));
            DataBufferUtils.release(buffer);
            return nettyDataBufferFactory.wrap(bytes);
        } catch (IOException e) {
            e.printStackTrace();

        }

        return null;

    }
}
