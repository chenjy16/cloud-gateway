package org.cloud.gateway.transport.netty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Created by cjy on 2020/1/24.
 */
@Singleton
public class ServerStatusManager
{
    private final ApplicationInfoManager applicationInfoManager;
    private final DiscoveryClient discoveryClient;

    @Inject
    public ServerStatusManager(ApplicationInfoManager applicationInfoManager, DiscoveryClient discoveryClient)
    {
        this.applicationInfoManager = applicationInfoManager;
        this.discoveryClient = discoveryClient;
    }

    public InstanceInfo.InstanceStatus status() {

        // NOTE: when debugging this locally, found to my surprise that when the instance is maked OUT_OF_SERVICE remotely
        // in Discovery, although the StatusChangeEvent does get fired, the _local_ InstanceStatus (ie.
        // applicationInfoManager.getInfo().getStatus()) does not get changed to reflect that.
        // So that's why I'm doing this little dance here of looking at both remote and local statuses.

        InstanceInfo.InstanceStatus local = localStatus();
        InstanceInfo.InstanceStatus remote = remoteStatus();

        if (local == UP && remote != UNKNOWN) {
            return remote;
        }
        else {
            return local;
        }
    }

    public InstanceInfo.InstanceStatus localStatus() {
        return applicationInfoManager.getInfo().getStatus();
    }

    public InstanceInfo.InstanceStatus remoteStatus() {
        return discoveryClient.getInstanceRemoteStatus();
    }

    public void localStatus(InstanceInfo.InstanceStatus status) {
        applicationInfoManager.setInstanceStatus(status);
    }

    public int health() {
        // TODO
        throw new UnsupportedOperationException();
    }
}
