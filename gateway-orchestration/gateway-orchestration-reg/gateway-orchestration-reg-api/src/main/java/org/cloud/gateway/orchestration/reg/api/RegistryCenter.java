package org.cloud.gateway.orchestration.reg.api;

import org.cloud.gateway.orchestration.reg.listener.DataChangedEventListener;

import java.util.List;


public interface RegistryCenter extends AutoCloseable {
    

    void init(RegistryCenterConfiguration config);
    

    String get(String key);
    

    String getDirectly(String key);
    

    boolean isExisted(String key);
    

    List<String> getChildrenKeys(String key);
    

    void persist(String key, String value);
    

    void update(String key, String value);
    

    void persistEphemeral(String key, String value);
    

    void watch(String key, DataChangedEventListener dataChangedEventListener);
}
