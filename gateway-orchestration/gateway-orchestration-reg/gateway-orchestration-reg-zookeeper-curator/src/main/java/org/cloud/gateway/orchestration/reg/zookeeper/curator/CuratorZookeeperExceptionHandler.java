package org.cloud.gateway.orchestration.reg.zookeeper.curator;
import org.cloud.gateway.orchestration.reg.exception.RegistryCenterException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CuratorZookeeperExceptionHandler {
    

    public static void handleException(final Exception cause) {
        if (null == cause) {
            return;
        }
        if (isIgnoredException(cause) || null != cause.getCause() && isIgnoredException(cause.getCause())) {
            log.debug("Ignored exception for: {}", cause.getMessage());
        } else if (cause instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new RegistryCenterException(cause);
        }
    }
    
    private static boolean isIgnoredException(final Throwable cause) {
        return cause instanceof ConnectionLossException || cause instanceof NoNodeException || cause instanceof NodeExistsException;
    }
}
