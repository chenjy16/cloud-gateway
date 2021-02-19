
package org.cloud.gateway.orchestration.internal.registry;

import com.google.common.base.Preconditions;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenterConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;


@Slf4j
public final class RegistryCenterLoader {
    

    public static RegistryCenter load(final RegistryCenterConfiguration regCenterConfig) {
        Preconditions.checkNotNull(regCenterConfig, "Registry center configuration cannot be null.");
        RegistryCenter result = null;
        int count = 0;
        for (RegistryCenter each : ServiceLoader.load(RegistryCenter.class)) {
            result = each;
            count++;
        }
        Preconditions.checkNotNull(result, "Cannot load implementation class for registry center.");
        if (1 != count) {
            log.warn("Find more than one registry center implementation class, use `{}` now.", result.getClass().getName());
        }
        result.init(regCenterConfig);
        return result;
    }
}
