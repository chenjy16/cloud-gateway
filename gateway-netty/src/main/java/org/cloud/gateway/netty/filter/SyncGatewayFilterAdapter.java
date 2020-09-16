package org.cloud.gateway.netty.filter;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.GatewayMessage;
import rx.Observable;

import static org.cloud.gateway.netty.filter.FilterSyncType.SYNC;
import static org.cloud.gateway.netty.filter.FilterType.ENDPOINT;


public abstract class SyncGatewayFilterAdapter<I extends GatewayMessage, O extends GatewayMessage> implements SyncGatewayFilter<I, O> {


    @Override
    public boolean isDisabled() {
        return false;
    }



    @Override
    public int filterOrder() {
        // Set all Endpoint filters to order of 0, because they are not processed sequentially like other filter types.
        return 0;
    }

    @Override
    public FilterType filterType() {
        return ENDPOINT;
    }

    @Override
    public boolean overrideStopFilterProcessing() {
        return false;
    }

    @Override
    public Observable<O> applyAsync(I input) {
        return Observable.just(apply(input));
    }

    @Override
    public FilterSyncType getSyncType() {
        return SYNC;
    }

    @Override
    public boolean needsBodyBuffered(I input) {
        return false;
    }

    @Override
    public HttpContent processContentChunk(GatewayMessage zuulMessage, HttpContent chunk) {
        return chunk;
    }

    @Override
    public void incrementConcurrency() {
        //NOOP for sync filters
    }

    @Override
    public void decrementConcurrency() {
        //NOOP for sync filters
    }


}
