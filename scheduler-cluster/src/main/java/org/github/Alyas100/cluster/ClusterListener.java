package org.github.Alyas100.cluster;

/**
 * Listener for cluster events.
 */
public interface ClusterListener {
    default void onLeaderElected(String leaderId) {}
    default void onNodeJoined(ClusterNode node) {}
    default void onNodeLeft(ClusterNode node) {}
    default void onJobDistributed(String jobId, String targetNode) {}
}