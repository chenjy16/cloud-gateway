package org.cloud.gateway.netty.filter;
import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.GatewayMessage;
import org.cloud.gateway.message.HttpRequestMessage;
import org.cloud.gateway.netty.service.SessionContext;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseGatewayFilterRunner<I extends GatewayMessage, O extends GatewayMessage> implements FilterRunner<I, O>{

    private final FilterRunner<O, ? extends GatewayMessage> nextStage;
    private final String RUNNING_FILTER_IDX_SESSION_CTX_KEY;
    private final String AWAITING_BODY_FLAG_SESSION_CTX_KEY;

    public BaseGatewayFilterRunner(FilterRunner<O, ? extends GatewayMessage> nextStage, FilterType filterType) {
        this.nextStage = nextStage;
        this.RUNNING_FILTER_IDX_SESSION_CTX_KEY = filterType + "RunningFilterIndex";
        this.AWAITING_BODY_FLAG_SESSION_CTX_KEY = filterType + "IsAwaitingBody";
    }






    public  final O filter(final GatewayFilter<I, O> filter, final I inMesg) {

        //这里是关键方法。如果filter需要对body进行处理，那就需要检查body是否全部读完，
        // 如果因为半包等原因没有读完，那需要等，这里等不是说线程会block,
        // 而且等底层io读事件触发后继续处理。这里返回null很重要，
        // filter chain 就不会执行后面的filter了，并做个标记在请求上下文。
        if (!isMessageBodyReadyForFilter(filter, inMesg)) {
            setFilterAwaitingBody(inMesg, true);
            return null;  //wait for whole body to be buffered
        }
        //如果不需要body，则把标志清楚，因为有可能是上次等待设置的，现在来了，就可以清楚了
        setFilterAwaitingBody(inMesg, false);


        //run body contents accumulated so far through this filter
        //如果是http request header 部分请求request filter都执行完了，到endpoint 执行时会啥都不做。
        //而是通过下面的apply方法来建立连接和后端，并发送请求相关的信息。
        inMesg.runBufferedBodyContentThroughFilter(filter);

        if (filter.getSyncType() == FilterSyncType.SYNC) {
            final SyncGatewayFilter<I, O> syncFilter = (SyncGatewayFilter) filter;
            final O outMesg;
            //proxyendpoint
            outMesg = syncFilter.apply(inMesg);
            return (outMesg != null) ? outMesg : filter.getDefaultOutput(inMesg);
        }
        return null;

    }





    private boolean isMessageBodyReadyForFilter(final GatewayFilter filter, final I inMesg) {
        return ((!filter.needsBodyBuffered(inMesg)) || (inMesg.hasCompleteBody()));
    }


    protected final void setFilterAwaitingBody(I zuulMesg, boolean flag) {
        if (flag) {
            zuulMesg.getContext().put(AWAITING_BODY_FLAG_SESSION_CTX_KEY, Boolean.TRUE);
        }
        else {
            zuulMesg.getContext().remove(AWAITING_BODY_FLAG_SESSION_CTX_KEY);
        }
    }


    protected final void invokeNextStage(final O gwMesg) {


        if (nextStage != null) {
                nextStage.filter(gwMesg);
        } else {
                //getChannelHandlerContext(zuulMesg).fireChannelRead(zuulMesg);
        }





    }


    protected final void invokeNextStage(final O zuulMesg, final HttpContent chunk) {

        nextStage.filter(zuulMesg, chunk);
    }




    protected final AtomicInteger getRunningFilterIndex(I zuulMesg) {
        final SessionContext ctx = zuulMesg.getContext();
        return (AtomicInteger) Preconditions.checkNotNull(ctx.get(RUNNING_FILTER_IDX_SESSION_CTX_KEY), "runningFilterIndex");
    }


    protected final boolean isFilterAwaitingBody(I zuulMesg) {
        return zuulMesg.getContext().containsKey("AWAITING_BODY_FLAG_SESSION_CTX_KEY");
    }

    protected final AtomicInteger initRunningFilterIndex(I zuulMesg) {
        final AtomicInteger idx = new AtomicInteger(0);
        zuulMesg.getContext().put(RUNNING_FILTER_IDX_SESSION_CTX_KEY, idx);
        return idx;
    }
}
