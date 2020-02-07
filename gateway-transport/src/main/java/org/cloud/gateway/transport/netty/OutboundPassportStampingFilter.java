package org.cloud.gateway.transport.netty;

import static com.netflix.zuul.filters.FilterType.OUTBOUND;

import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.passport.PassportStampingFilter;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.passport.PassportState;

public final class OutboundPassportStampingFilter extends PassportStampingFilter<HttpResponseMessage> {

    public OutboundPassportStampingFilter(PassportState stamp) {
        super(stamp);
    }

    @Override
    public FilterType filterType() {
        return OUTBOUND;
    }

}
