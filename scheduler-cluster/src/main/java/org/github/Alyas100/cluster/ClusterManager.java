package org.github.Alyas100.cluster;

import org.github.Alyas100.JobDefinition;
import java.util.Set;

/**
 * Manages cluster coordination and distributed job scheduling.
 */
public interface ClusterManager {
    // Cluster Management
    void start();
    void stop();
    boolean isLeader();
    String getNodeId();
    Set<ClusterNode> getClusterNodes();

    // Job Distribution
    void distributeJob(JobDefinition job);
    void rebalanceJobs();

    // Event Listeners
    void addClusterListener(ClusterListener listener);
    void removeClusterListener(ClusterListener listener);

    // Cluster State
    ClusterState getClusterState();  // ← Uses OUR ClusterState, not Hazelcast's
}