package org.cloud.gateway.admin.controller;
import lombok.extern.slf4j.Slf4j;
import org.cloud.gateway.admin.response.GatewayAdminRes;
import org.cloud.gateway.admin.response.ResponseResultUtil;
import org.cloud.gateway.admin.service.PluginConfigurationService;
import org.cloud.gateway.core.configuration.PluginConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;


@RestController
@RequestMapping("/plugin")
@Slf4j
public class PluginController {

    @Autowired
    private PluginConfigurationService pluginConfigurationService;


    @GetMapping("")
    public Mono<GatewayAdminRes> queryPlugins(){
        try {
            List<PluginConfiguration> list=pluginConfigurationService.findPluginRules();
            return Mono.create(sink->sink.success(ResponseResultUtil.build(list)));
        }  catch (Exception e)  {
            log.error("查询插件列表报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }

    }


    @GetMapping("/{id}")
    public Mono<GatewayAdminRes> detailPlugin(@PathVariable("id") final String id) {
        try {
            PluginConfiguration pluginConfiguration=pluginConfigurationService.findPluginRuleById(id);
            return Mono.create(sink->sink.success(ResponseResultUtil.build(pluginConfiguration)));
        }  catch (Exception e)  {
            log.error("查询插件报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }

    }


    @PostMapping("")
    public Mono<GatewayAdminRes> createPlugin() {
        try {
            pluginConfigurationService.persistPluginRule(null);
            return Mono.create(sink->sink.success(ResponseResultUtil.success()));
        } catch (Exception e) {
            log.error("创建插件报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }
    }


    @PutMapping("/{id}")
    public Mono<GatewayAdminRes> updatePlugin(@PathVariable("id") final String id){
        try {
            pluginConfigurationService.persistPluginRule(null);
            return Mono.create(sink->sink.success(ResponseResultUtil.success()));
        } catch (Exception e) {
            log.error("更新插件报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }
    }

    @DeleteMapping("/{id}")
    public Mono<GatewayAdminRes> deletePlugins(@PathVariable("id") final String id){
        try {
            pluginConfigurationService.deletePluginRule(id);
            return Mono.create(sink->sink.success(ResponseResultUtil.success()));
        } catch (Exception e) {
            log.error("删除插件报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }
    }

}
