package org.cloud.gateway.admin.config;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenterConfiguration;
import org.cloud.gateway.orchestration.reg.zookeeper.curator.CuratorZookeeperRegistryCenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.cloud.gateway.admin")
public class GatewayAdminConfig {

    @Bean
    public RegistryCenter registryCenter(RegistryCenterConfiguration registryCenterConfiguration) {
        CuratorZookeeperRegistryCenter czrc=new CuratorZookeeperRegistryCenter();
        czrc.init(registryCenterConfiguration);
        return czrc;
    }

    @Bean
    public RegistryCenterConfiguration registryCenterConfiguration() {
        return new RegistryCenterConfiguration();
    }

}
