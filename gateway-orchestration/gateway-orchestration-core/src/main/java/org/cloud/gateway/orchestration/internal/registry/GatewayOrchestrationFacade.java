package org.cloud.gateway.orchestration.internal.registry;
import lombok.Getter;
import org.cloud.gateway.orchestration.config.OrchestrationConfiguration;
import org.cloud.gateway.orchestration.internal.registry.config.service.ConfigurationService;
import org.cloud.gateway.orchestration.internal.registry.listener.GatewayOrchestrationListenerManager;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import lombok.extern.slf4j.Slf4j;
import java.util.Collection;


@Slf4j
public final class GatewayOrchestrationFacade implements AutoCloseable {
    
    private final RegistryCenter regCenter;

    @Getter
    private final ConfigurationService configService;


    private final GatewayOrchestrationListenerManager listenerManager;
    
    public GatewayOrchestrationFacade(final OrchestrationConfiguration orchestrationConfig) {

        regCenter = RegistryCenterLoader.load(orchestrationConfig.getRegCenterConfig());

        configService = new ConfigurationService(orchestrationConfig.getName(), regCenter);

        listenerManager = new GatewayOrchestrationListenerManager(orchestrationConfig.getName(), regCenter);

    }


    public void init() {
        listenerManager.initListeners();
    }
    
    @Override
    public void close() {
        try {
            regCenter.close();
        } catch (final Exception ex) {
            log.warn("RegCenter exception for: {}", ex.getMessage());
        }
    }
}
