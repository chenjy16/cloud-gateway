package org.cloud.gateway.admin.controller;
import org.cloud.gateway.admin.response.GatewayAdminRes;
import org.cloud.gateway.admin.service.RouteConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/routeRule")
public class RouteRuleController {

    @Autowired
    private RouteConfigurationService routeConfigurationService;

    @GetMapping("")
    public Mono<GatewayAdminRes> queryRules(){
        return Mono.create(sink->sink.success(new GatewayAdminRes()));
    }

    @GetMapping("/{id}")
    public Mono<GatewayAdminRes> detailRule(@PathVariable("id") final String id) {
        return Mono.create(sink->sink.success(new GatewayAdminRes()));
    }


    @PostMapping("")
    public Mono<GatewayAdminRes> createRule() {
        return Mono.create(sink->sink.success(new GatewayAdminRes()));

    }

    @PutMapping("/{id}")
    public Mono<GatewayAdminRes> updateRule(@PathVariable("id") final String id) {

        return Mono.create(sink->sink.success(new GatewayAdminRes()));
    }


    @DeleteMapping("/batch")
    public Mono<GatewayAdminRes> deleteRules(@RequestBody final List<String> ids) {
        return Mono.create(sink->sink.success(new GatewayAdminRes()));
    }


}
