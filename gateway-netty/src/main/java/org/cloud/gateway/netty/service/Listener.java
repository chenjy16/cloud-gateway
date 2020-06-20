package org.cloud.gateway.netty.service;

public interface Listener {
    void onSuccess(Object... args);

    void onFailure(Throwable cause);
}