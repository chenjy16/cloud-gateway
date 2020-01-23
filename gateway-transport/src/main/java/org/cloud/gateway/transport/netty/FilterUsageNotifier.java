package org.cloud.gateway.transport.netty;

/**
 * Created by cjy on 2020/1/6.
 */
public interface FilterUsageNotifier {
    public void notify(ZuulFilter filter, ExecutionStatus status);
}
