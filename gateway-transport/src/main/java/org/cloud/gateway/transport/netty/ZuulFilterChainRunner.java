package org.cloud.gateway.transport.netty;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import io.netty.handler.codec.http.HttpContent;
import io.perfmark.PerfMark;

@ThreadSafe
public class ZuulFilterChainRunner<T extends ZuulMessage> extends BaseZuulFilterRunner<T, T> {

    private final ZuulFilter<T, T>[] filters;

    public ZuulFilterChainRunner(ZuulFilter<T, T>[] zuulFilters, FilterUsageNotifier usageNotifier, FilterRunner<T, ?> nextStage) {
        super(zuulFilters[0].filterType(), usageNotifier, nextStage);
        this.filters = zuulFilters;
    }

    public ZuulFilterChainRunner(ZuulFilter<T, T>[] zuulFilters, FilterUsageNotifier usageNotifier) {
        this(zuulFilters, usageNotifier, null);
    }

    @Override
    public void filter(final T inMesg) {
        PerfMark.startTask(getClass().getSimpleName(), "filter");
        try {
            addPerfMarkTags(inMesg);
            runFilters(inMesg, initRunningFilterIndex(inMesg));
        } finally {
            PerfMark.stopTask(getClass().getSimpleName(), "filter");
        }
    }

    @Override
    protected void resume(final T inMesg) {
        PerfMark.startTask(getClass().getSimpleName(), "resume");
        try {
            final AtomicInteger runningFilterIdx = getRunningFilterIndex(inMesg);
            runningFilterIdx.incrementAndGet();
            runFilters(inMesg, runningFilterIdx);
        } finally {
            PerfMark.stopTask(getClass().getSimpleName(), "resume");
        }
    }

    private final void runFilters(final T mesg, final AtomicInteger runningFilterIdx) {
        T inMesg = mesg;
        String filterName = "-";
        try {
            Preconditions.checkNotNull(mesg, "Input message");
            int i = runningFilterIdx.get();

            while (i < filters.length) {
                final ZuulFilter<T, T> filter = filters[i];
                filterName = filter.filterName();
                final T outMesg = filter(filter, inMesg);
                if (outMesg == null) {
                    return; //either async filter or waiting for the message body to be buffered
                }
                inMesg = outMesg;
                i = runningFilterIdx.incrementAndGet();
            }

            //Filter chain has reached its end, pass result to the next stage
            invokeNextStage(inMesg);
        }
        catch (Exception ex) {
            handleException(inMesg, filterName, ex);
        }
    }

    @Override
    public void filter(T inMesg, HttpContent chunk) {
        String filterName = "-";
        PerfMark.startTask(getClass().getName(), "filterChunk");
        try {
            addPerfMarkTags(inMesg);
            Preconditions.checkNotNull(inMesg, "input message");

            final AtomicInteger runningFilterIdx = getRunningFilterIndex(inMesg);
            final int limit = runningFilterIdx.get();
            for (int i = 0; i < limit; i++) {
                final ZuulFilter<T, T> filter = filters[i];
                filterName = filter.filterName();
                if ((! filter.isDisabled()) && (! shouldSkipFilter(inMesg, filter))) {
                    final HttpContent newChunk = filter.processContentChunk(inMesg, chunk);
                    if (newChunk == null)  {
                        //Filter wants to break the chain and stop propagating this chunk any further
                        return;
                    }
                    //deallocate original chunk if necessary
                    if ((newChunk != chunk) && (chunk.refCnt() > 0)) {
                        chunk.release(chunk.refCnt());
                    }
                    chunk = newChunk;
                }
            }

            if (limit >= filters.length) {
                //Filter chain has run to end, pass down the channel pipeline
                invokeNextStage(inMesg, chunk);
            } else {
                inMesg.bufferBodyContents(chunk);

                boolean isAwaitingBody = isFilterAwaitingBody(inMesg);

                // Record passport states for start and end of buffering bodies.
                if (isAwaitingBody) {
                    CurrentPassport passport = CurrentPassport.fromSessionContext(inMesg.getContext());
                    if (inMesg.hasCompleteBody()) {
                        if (inMesg instanceof HttpRequestMessage) {
                            passport.addIfNotAlready(PassportState.FILTERS_INBOUND_BUF_END);
                        } else if (inMesg instanceof HttpResponseMessage) {
                            passport.addIfNotAlready(PassportState.FILTERS_OUTBOUND_BUF_END);
                        }
                    }
                    else {
                        if (inMesg instanceof HttpRequestMessage) {
                            passport.addIfNotAlready(PassportState.FILTERS_INBOUND_BUF_START);
                        } else if (inMesg instanceof HttpResponseMessage) {
                            passport.addIfNotAlready(PassportState.FILTERS_OUTBOUND_BUF_START);
                        }
                    }
                }

                if (isAwaitingBody && inMesg.hasCompleteBody()) {
                    //whole body has arrived, resume filter chain
                    runFilters(inMesg, runningFilterIdx);
                }
            }
        }
        catch (Exception ex) {
            handleException(inMesg, filterName, ex);
        } finally {
            PerfMark.stopTask(getClass().getName(), "filterChunk");
        }
    }

}
