package org.cloud.gateway.core.algorithm;

import java.util.List;

public interface LoadBalanceAlgorithm {

    public String getDataSource(final String name, final String masterDataSourceName, final List<String> slaveDataSourceNames);


}
