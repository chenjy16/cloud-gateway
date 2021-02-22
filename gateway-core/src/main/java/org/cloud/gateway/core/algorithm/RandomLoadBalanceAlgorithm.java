package org.cloud.gateway.core.algorithm;
import java.util.List;
import java.util.Random;


public final class RandomLoadBalanceAlgorithm implements LoadBalanceAlgorithm {
    
    @Override
    public String getDataSource(final String name, final String masterDataSourceName, final List<String> slaveDataSourceNames) {
        return slaveDataSourceNames.get(new Random().nextInt(slaveDataSourceNames.size()));
    }
}
