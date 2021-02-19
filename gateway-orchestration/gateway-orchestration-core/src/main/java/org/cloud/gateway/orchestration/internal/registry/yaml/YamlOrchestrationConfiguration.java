package org.cloud.gateway.orchestration.internal.registry.yaml;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.cloud.gateway.orchestration.config.OrchestrationConfiguration;
import org.cloud.gateway.orchestration.reg.api.RegistryCenterConfiguration;


@Getter
@Setter
public class YamlOrchestrationConfiguration {
    
    private String name;
    
    private RegistryCenterConfiguration registry;
    
    private boolean overwrite;
    

    public OrchestrationConfiguration getOrchestrationConfiguration() {
        Preconditions.checkNotNull(registry, "Registry center must be required.");
        return new OrchestrationConfiguration(name, registry, overwrite);
    }
}
