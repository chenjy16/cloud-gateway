package org.cloud.gateway.transport.netty;

import java.util.Optional;

import com.netflix.zuul.message.Headers;
import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.message.http.Cookies;
import com.netflix.zuul.message.http.HttpQueryParams;

public interface HttpRequestInfo extends ZuulMessage
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

    /**
     * Reflects the actual destination port that the client intended to communicate with,
     * in preference to the port Zuul was listening on. In the case where proxy protocol is
     * enabled, this should reflect the destination IP encoded in the TCP payload by the load balancer.
     */
    default Optional<Integer> getClientDestinationPort() {
        throw new UnsupportedOperationException();
    }

    String reconstructURI();

    /** Parse and lazily cache the request cookies. */
    Cookies parseCookies();

    /**
     * Force parsing/re-parsing of the cookies. May want to do this if headers
     * have been mutated since cookies were first parsed.
     */
    Cookies reParseCookies();
}

