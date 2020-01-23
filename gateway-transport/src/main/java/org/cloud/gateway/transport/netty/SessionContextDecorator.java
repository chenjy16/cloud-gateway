package org.cloud.gateway.transport.netty;

/**
 * Created by cjy on 2020/1/5.
 */
public interface SessionContextDecorator {
    public SessionContext decorate(SessionContext ctx);
}
