package org.cloud.gateway.transport.webflux.balance.factory;

import org.cloud.gateway.transport.webflux.balance.LoadBalance;
import org.cloud.gateway.transport.webflux.balance.spi.RandomLoadBalance;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;


public class LoadBalanceFactory {

    private static final ServiceLoader<LoadBalance> SERVICE_LOADER =
            SpiLoadFactory.loadAll(LoadBalance.class);


    public static LoadBalance of(final String algorithm) {
        return StreamSupport.stream(SERVICE_LOADER.spliterator(), false)
                .filter(service ->
                        Objects.equals(service.algorithm(),
                                algorithm)).findFirst().orElse(new RandomLoadBalance());
    }
}
