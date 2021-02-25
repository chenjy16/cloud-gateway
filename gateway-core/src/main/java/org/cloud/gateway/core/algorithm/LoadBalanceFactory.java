package org.cloud.gateway.core.algorithm;
import org.cloud.gateway.core.utils.SpiLoadFactory;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public class LoadBalanceFactory {
    private static final ServiceLoader<LoadBalanceAlgorithm> SERVICE_LOADER =SpiLoadFactory.loadAll(LoadBalanceAlgorithm.class);




    public static LoadBalanceAlgorithm of(final String algorithm) {
        return StreamSupport.stream(SERVICE_LOADER.spliterator(), false)
                .filter(service ->Objects.equals(service.algorithm(), algorithm)).findFirst().orElse(new RoundRobinLoadBalanceAlgorithm());
    }

}
