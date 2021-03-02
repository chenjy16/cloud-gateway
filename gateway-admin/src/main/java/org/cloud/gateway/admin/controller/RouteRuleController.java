package org.cloud.gateway.admin.controller;
import lombok.extern.slf4j.Slf4j;
import org.cloud.gateway.admin.response.GatewayAdminRes;
import org.cloud.gateway.admin.response.ResponseResultUtil;
import org.cloud.gateway.admin.service.RouteConfigurationService;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/routeRule")
@Slf4j
public class RouteRuleController {

    @Autowired
    private RouteConfigurationService routeConfigurationService;

    @GetMapping("")
    public Mono<GatewayAdminRes> queryRules(){
        try {
            List<ClusterConfiguration> list=routeConfigurationService.findRouteRules();
            return Mono.create(sink->sink.success(ResponseResultUtil.build(list)));
        }  catch (Exception e)  {
            log.error("查询路由规则列表报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }
    }

    @GetMapping("/{id}")
    public Mono<GatewayAdminRes> detailRule(@PathVariable("id") final String id) {
        try {
            ClusterConfiguration clusterConfiguration=routeConfigurationService.findRouteRuleById(id);
            return Mono.create(sink->sink.success(ResponseResultUtil.build(clusterConfiguration)));
        }  catch (Exception e)  {
            log.error("查询路由规则报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }
    }


    @PostMapping("")
    public Mono<GatewayAdminRes> createRule() {
        try {
            routeConfigurationService.persistRouteRule(null);
            return Mono.create(sink->sink.success(ResponseResultUtil.success()));
        }  catch (Exception e)  {
            log.error("创建路由规则报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }

    }

    @PutMapping("/{id}")
    public Mono<GatewayAdminRes> updateRule(@PathVariable("id") final String id) {
        try {
            routeConfigurationService.persistRouteRule(null);
            return Mono.create(sink->sink.success(ResponseResultUtil.success()));
        }  catch (Exception e)  {
            log.error("更新路由规则报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }
    }


    @DeleteMapping("/{id}")
    public Mono<GatewayAdminRes> deleteRules(@RequestBody final String id) {
        try {
            routeConfigurationService.deleteRouteRule(id);
            return Mono.create(sink->sink.success(ResponseResultUtil.success()));
        }  catch (Exception e)  {
            log.error("删除路由规则报错:{}",e);
            return Mono.create(sink->sink.error(e));
        }
    }


}
