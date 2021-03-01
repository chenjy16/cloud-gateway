package org.cloud.gateway.admin.controller;
import org.cloud.gateway.admin.response.GatewayAdminRes;
import org.cloud.gateway.admin.service.PluginConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;


@RestController
@RequestMapping("/plugin")
public class PluginController {


    @Autowired
    private PluginConfigurationService pluginConfigurationService;

    @GetMapping("")
    public Mono<GatewayAdminRes> queryPlugins(){

        return Mono.create(sink->sink.success(new GatewayAdminRes()));
    }


    @GetMapping("/{id}")
    public Mono<GatewayAdminRes> detailPlugin(@PathVariable("id") final String id) {
        pluginConfigurationService.findPluginRuleById(id);
        return Mono.create(sink->sink.success(new GatewayAdminRes()));

    }


    @PostMapping("")
    public Mono<GatewayAdminRes> createPlugin() {
        pluginConfigurationService.persistPluginRule(null);
        return Mono.create(sink->sink.success(new GatewayAdminRes()));
    }


    @PutMapping("/{id}")
    public Mono<GatewayAdminRes> updatePlugin(@PathVariable("id") final String id){
        pluginConfigurationService.persistPluginRule(null);
        return Mono.create(sink->sink.success(new GatewayAdminRes()));
    }

    @DeleteMapping("/{id}")
    public Mono<GatewayAdminRes> deletePlugins(@PathVariable("id") final String id){
        pluginConfigurationService.deletePluginRule(id);
        return Mono.create(sink->sink.success(new GatewayAdminRes()));
    }

}
