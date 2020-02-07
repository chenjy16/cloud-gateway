package org.cloud.gateway.transport.netty;

import static com.netflix.zuul.filters.FilterType.INBOUND;

import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.passport.PassportStampingFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.passport.PassportState;

public final class InboundPassportStampingFilter extends PassportStampingFilter<HttpRequestMessage> {

    public InboundPassportStampingFilter(PassportState stamp) {
        super(stamp);
    }

    @Override
    public FilterType filterType() {
        return INBOUND;
    }

}
