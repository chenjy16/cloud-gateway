package org.cloud.gateway.orchestration.reg.api;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public final class RegistryCenterConfiguration {
    

    private String serverLists;
    

    private String namespace;
    

    private String digest;
    

    private int operationTimeoutMilliseconds = 500;
    

    private int maxRetries = 3;
    

    private int retryIntervalMilliseconds = 500;
    

    private int timeToLiveSeconds = 60;
}
