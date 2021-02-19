package org.cloud.gateway.transport.webflux.handler;
import com.google.common.collect.Maps;
import org.cloud.gateway.common.exception.CommonErrorCode;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import java.util.Map;


public class GlobalErrorHandler extends DefaultErrorWebExceptionHandler {


    public GlobalErrorHandler(final ErrorAttributes errorAttributes,
                              final ResourceProperties resourceProperties,
                              final ErrorProperties errorProperties,
                              final ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(final ServerRequest request, final boolean includeStackTrace) {
        Throwable error = super.getError(request);
        error.printStackTrace();
        return response(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected HttpStatus getHttpStatus(final Map<String, Object> errorAttributes) {
        int statusCode = (int) errorAttributes.get("code");
        return HttpStatus.valueOf(statusCode);
    }

    private static Map<String, Object> response(final int status) {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(3);
        map.put("code", status);
        map.put("message", CommonErrorCode.ERROR_MSG);
        map.put("data", null);
        return map;
    }

}


