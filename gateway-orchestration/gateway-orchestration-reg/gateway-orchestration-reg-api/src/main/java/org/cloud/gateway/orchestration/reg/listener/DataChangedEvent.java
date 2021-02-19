
package org.cloud.gateway.orchestration.reg.listener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class DataChangedEvent {
    
    private final String key;
    
    private final String value;
    
    private final ChangedType changedType;
    

    public enum ChangedType {
        
        UPDATED, DELETED, IGNORED
    }
}
