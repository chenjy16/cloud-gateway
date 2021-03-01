package org.cloud.gateway.admin.response;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class GatewayAdminRes {

    private Integer code;

    private String message;

    private Object data;
}
