package org.cloud.gateway.core.configuration;
import lombok.Data;

@Data
public class ServerConfiguration {
    private String  id;
    private String  addr;
    private String  protocol;
    private String  weight;

}
