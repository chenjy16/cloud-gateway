package org.cloud.gateway.netty.filter;
import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.ZuulMessage;
import org.cloud.gateway.netty.service.SessionContext;

import java.util.concurrent.atomic.AtomicInteger;

abstract class BaseZuulFilterRunner<I extends ZuulMessage, O extends ZuulMessage> implements FilterRunner<I, O>{

    private final FilterRunner<O, ? extends ZuulMessage> nextStage;
    private final String RUNNING_FILTER_IDX_SESSION_CTX_KEY;
    private final String AWAITING_BODY_FLAG_SESSION_CTX_KEY;

    public BaseZuulFilterRunner(FilterRunner<O, ? extends ZuulMessage> nextStage,FilterType filterType) {
        this.nextStage = nextStage;
        this.RUNNING_FILTER_IDX_SESSION_CTX_KEY = filterType + "RunningFilterIndex";
        this.AWAITING_BODY_FLAG_SESSION_CTX_KEY = filterType + "IsAwaitingBody";
    }

    public  final O filter(final GatewayFilter<I, O> filter, final I inMesg) {


        if (filter.getSyncType() == FilterSyncType.SYNC) {
            final SyncGatewayFilter<I, O> syncFilter = (SyncGatewayFilter) filter;
            final O outMesg;
            //proxyendpoint
            outMesg = syncFilter.apply(inMesg);
            return (outMesg != null) ? outMesg : filter.getDefaultOutput(inMesg);
        }
        return null;
    }


    protected final void invokeNextStage(final O zuulMesg) {

        nextStage.filter(zuulMesg);


    }


    protected final void invokeNextStage(final O zuulMesg, final HttpContent chunk) {

        nextStage.filter(zuulMesg, chunk);
    }




    protected final AtomicInteger getRunningFilterIndex(I zuulMesg) {
        final SessionContext ctx = zuulMesg.getContext();
        return (AtomicInteger) Preconditions.checkNotNull(ctx.get(RUNNING_FILTER_IDX_SESSION_CTX_KEY), "runningFilterIndex");
    }

}
