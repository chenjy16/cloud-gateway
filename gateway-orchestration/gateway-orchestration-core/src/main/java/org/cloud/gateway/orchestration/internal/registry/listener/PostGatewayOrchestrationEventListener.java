package org.cloud.gateway.orchestration.internal.registry.listener;

import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import org.cloud.gateway.orchestration.internal.registry.eventbus.GatewayOrchestrationEventBus;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.cloud.gateway.orchestration.reg.listener.DataChangedEvent;
import org.cloud.gateway.orchestration.reg.listener.DataChangedEventListener;

import java.util.Arrays;
import java.util.Collection;


@RequiredArgsConstructor
public abstract class PostGatewayOrchestrationEventListener implements GatewayOrchestrationListener {
    
    private final EventBus eventBus = GatewayOrchestrationEventBus.getInstance();
    
    private final RegistryCenter regCenter;
    
    private final String watchKey;
    
    @Override
    public final void watch(final DataChangedEvent.ChangedType... watchedChangedTypes) {
        final Collection<DataChangedEvent.ChangedType> watchedChangedTypeList = Arrays.asList(watchedChangedTypes);
        regCenter.watch(watchKey, new DataChangedEventListener() {
            
            @Override
            public void onChange(final DataChangedEvent dataChangedEvent) {
                if (watchedChangedTypeList.contains(dataChangedEvent.getChangedType())) {

                    eventBus.post(createShardingOrchestrationEvent(dataChangedEvent));
                }
            }
        });
    }
    
    protected abstract GatewayOrchestrationEvent createShardingOrchestrationEvent(DataChangedEvent event);
}
