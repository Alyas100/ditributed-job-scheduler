package org.github.Alyas100.web.dto;

import org.github.Alyas100.cluster.ClusterState;

public record ClusterInfo(
        String mode,
        String leaderId,
        int totalNodes,
        int activeNodes,
        boolean healthy,
        boolean hasQuorum
) {
    public static ClusterInfo fromClusterState(ClusterState clusterState) {
        return new ClusterInfo(
                "CLUSTER",
                clusterState.leaderId(),
                clusterState.totalNodes(),
                clusterState.activeNodes(),
                clusterState.healthy(),
                clusterState.hasQuorum()
        );
    }

    public static ClusterInfo singleNode() {
        return new ClusterInfo(
                "STANDALONE",
                "local",
                1,
                1,
                true,
                true
        );
    }
}