package org.cloud.gateway.admin.response;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@NoArgsConstructor
@Data
public final class GatewayAdminRes<T>  {

    private Integer code=0;

    private String message;

    private T data;

}
