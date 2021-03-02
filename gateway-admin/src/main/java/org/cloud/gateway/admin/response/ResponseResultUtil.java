package org.cloud.gateway.admin.response;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;



@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseResultUtil {
    

    public static GatewayAdminRes success() {
        return build(null);
    }
    

    public static <T> GatewayAdminRes<T> build(final T model) {
        GatewayAdminRes<T> result = new GatewayAdminRes<>();
        result.setCode(0);
        result.setData(model);
        return result;
    }

    

    public static GatewayAdminRes handleException(final Exception exception) {
        GatewayAdminRes result = new GatewayAdminRes<>();
        result.setCode(1);
        result.setMessage(exception.getMessage());
        return result;
    }
    

    
}
