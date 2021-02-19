
package org.cloud.gateway.orchestration.config;

import org.cloud.gateway.orchestration.reg.api.RegistryCenterConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public final class OrchestrationConfiguration {
    
    private final String name;
    
    private final RegistryCenterConfiguration regCenterConfig;
    
    private final boolean overwrite;
}
