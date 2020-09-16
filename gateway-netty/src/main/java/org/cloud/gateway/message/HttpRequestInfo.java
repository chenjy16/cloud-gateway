package org.cloud.gateway.message;
import io.netty.handler.codec.Headers;
import org.cloud.gateway.netty.service.HttpQueryParams;
import reactor.netty.http.Cookies;


public interface HttpRequestInfo extends GatewayMessage
{
    String getProtocol();

    String getMethod();

    String getPath();

    HttpQueryParams getQueryParams();

    String getPathAndQuery();

    Headers getHeaders();

    String getClientIp();

    String getScheme();

    int getPort();

    String getServerName();

    int getMaxBodySize();

    String getInfoForLogging();

    String getOriginalHost();

    String getOriginalScheme();

    String getOriginalProtocol();

    int getOriginalPort();

    String reconstructURI();

    /** Parse and lazily cache the request cookies. */
    Cookies parseCookies();

    /**
     * Force parsing/re-parsing of the cookies. May want to do this if headers
     * have been mutated since cookies were first parsed.
     */
    Cookies reParseCookies();
}
