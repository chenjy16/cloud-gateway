package org.cloud.gateway.netty.filter;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.HttpRequestMessage;
import org.cloud.gateway.message.ZuulMessage;
import java.util.concurrent.atomic.AtomicInteger;


public class GatewayFilterChainRunner<T extends ZuulMessage> extends BaseZuulFilterRunner<T, T>{

    private final GatewayFilter<T, T>[] filters;


    public GatewayFilterChainRunner(FilterRunner<T, ? extends ZuulMessage> nextStage, GatewayFilter<T, T>[] filters) {
        super(nextStage,null);
        this.filters = filters;
    }




    @Override
    public void filter(final T inMesg) {
            runFilters(inMesg,null);
    }



    @Override
    public void filter(T inMesg, HttpContent chunk) {

        final AtomicInteger runningFilterIdx = getRunningFilterIndex(inMesg);
        final int limit = runningFilterIdx.get();
        if (limit >= filters.length) {
            //Filter chain has run to end, pass down the channel pipeline
            invokeNextStage(inMesg, chunk);
        } else {


            inMesg.bufferBodyContents(chunk);
            boolean isAwaitingBody = isFilterAwaitingBody(inMesg);
            // Record passport states for start and end of buffering bodies.
            if (isAwaitingBody && inMesg.hasCompleteBody()) {
                //whole body has arrived, resume filter chain
                runFilters(inMesg, runningFilterIdx);
            }

        }

    }




    private final void runFilters(final T mesg, final AtomicInteger runningFilterIdx) {
        T inMesg = mesg;
        int i = runningFilterIdx.get();
        while (i < filters.length) {

            final GatewayFilter<T, T> filter = filters[i];
            //过滤器处理的核心逻辑
            final T outMesg = filter(filter, inMesg);

        }
        //Filter chain has reached its end, pass result to the next stage
        // Filter 执行完后，进入到上面提到的endpoint处理。这里的
        // endPoint 是ZuulEndPointRunner。
        invokeNextStage(mesg);
    }







     final boolean isFilterAwaitingBody(HttpRequestMessage zuulMesg) {
        return zuulMesg.getContext().containsKey("AWAITING_BODY_FLAG_SESSION_CTX_KEY");
    }





}
