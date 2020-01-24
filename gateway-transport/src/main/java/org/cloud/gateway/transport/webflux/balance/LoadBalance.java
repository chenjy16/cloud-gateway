package org.cloud.gateway.transport.webflux.balance;

import org.cloud.gateway.common.dto.convert.DivideUpstream;

import java.util.List;


public interface LoadBalance {


    DivideUpstream select(List<DivideUpstream> upstreamList, String ip);


    String algorithm();
}
