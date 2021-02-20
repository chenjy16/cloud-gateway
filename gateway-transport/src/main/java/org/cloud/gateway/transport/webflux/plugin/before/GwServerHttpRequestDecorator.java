package org.cloud.gateway.transport.webflux.plugin.before;
import com.google.common.io.ByteStreams;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;
import java.io.IOException;
import java.io.InputStream;

import static reactor.core.scheduler.Schedulers.single;


public class GwServerHttpRequestDecorator extends ServerHttpRequestDecorator {
    private Flux<DataBuffer> body;

    public GwServerHttpRequestDecorator(ServerHttpRequest delegate) {
        super(delegate);

        Flux<DataBuffer> flux = super.getBody();

        if (requiresIsNeedBody(delegate.getMethod())){
            body=flux.publishOn(single()).map(dataBuffer ->handleStream(delegate,dataBuffer));
        }else{
            body=flux;
        }
    }


    private DataBuffer handleStream(ServerHttpRequest delegate,DataBuffer buffer){
        String path=delegate.getURI().getPath();

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

    private boolean requiresIsNeedBody(HttpMethod method){
        switch(method){
            case PUT:
            case POST:
            case PATCH:
                return true;
            default:
                return false;
        }
    }


    @Override
    public Flux<DataBuffer> getBody() {
        return body;
    }
}