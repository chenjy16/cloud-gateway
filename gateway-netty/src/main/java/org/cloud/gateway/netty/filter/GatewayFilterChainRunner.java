package org.cloud.gateway.netty.filter;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.GatewayMessage;
import java.util.concurrent.atomic.AtomicInteger;


public class GatewayFilterChainRunner<T extends GatewayMessage> extends BaseGatewayFilterRunner<T, T> {

    private final GatewayFilter<T, T>[] filters;


    public GatewayFilterChainRunner(FilterRunner<T, ? extends GatewayMessage> nextStage,
                                    GatewayFilter<T, T>[] gwfilters) {
        super(nextStage, gwfilters[0].filterType());
        this.filters = gwfilters;
    }


    public GatewayFilterChainRunner(GatewayFilter<T, T>[] gwfilters) {
        this(null, gwfilters);
    }


    @Override
    public void filter(final T inMesg) {
        runFilters(inMesg,initRunningFilterIndex(inMesg));
    }


    
    
    /**
     * @desc   
     * @author chenjianyu944@gmail.com
     * @date   2020/9/17 11:18
     **/
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



    /**
     * @desc
     * @author chenjianyu944@gmail.com
     * @date   2020/9/17 11:18
     **/
    private final void runFilters(final T mesg, final AtomicInteger runningFilterIdx) {
        T inMesg = mesg;
        int i = runningFilterIdx.get();
        while (i < filters.length) {
            final GatewayFilter<T, T> filter = filters[i];
            //过滤器处理的核心逻辑
            final T outMesg = filter(filter, inMesg);
            inMesg = outMesg;
            i = runningFilterIdx.incrementAndGet();
        }
        //Filter chain has reached its end, pass result to the next stage
        // Filter 执行完后，进入到上面提到的endpoint处理。这里的
        // endPoint 是ZuulEndPointRunner。
        invokeNextStage(mesg);
    }

}
