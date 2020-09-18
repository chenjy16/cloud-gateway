package org.cloud.gateway.netty.service;


public interface SessionContextDecorator {
    public SessionContext decorate(SessionContext ctx);
}