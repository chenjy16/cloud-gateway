package org.cloud.gateway.core.rule;
import lombok.Data;
import lombok.ToString;



@Data
@ToString
public class RouteRule {


    private String upstreamHost;


    private String protocol;


    private String upstreamUrl;


    private int weight;


}
