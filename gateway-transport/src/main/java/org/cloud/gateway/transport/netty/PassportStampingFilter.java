package org.cloud.gateway.transport.netty;

import com.netflix.zuul.filters.SyncZuulFilterAdapter;
import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.passport.CurrentPassport;
import com.netflix.zuul.passport.PassportState;

public abstract class PassportStampingFilter<T extends ZuulMessage> extends SyncZuulFilterAdapter<T, T> {

    private final PassportState stamp;
    private final String name;

    public PassportStampingFilter(PassportState stamp) {
        this.stamp = stamp;
        this.name = filterType().name()+"-"+stamp.name()+"-Filter";
    }

    @Override
    public String filterName() {
        return name;
    }

    @Override
    public T getDefaultOutput(T input) {
        return input;
    }

    @Override
    public T apply(T input) {
        CurrentPassport.fromSessionContext(input.getContext()).add(stamp);
        return input;
    }

}
