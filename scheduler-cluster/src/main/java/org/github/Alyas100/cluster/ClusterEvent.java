package org.github.Alyas100.cluster;

/**
 * Cluster events for communication between nodes.
 */
public sealed interface ClusterEvent {
    record NodeJoined(String nodeId) implements ClusterEvent {}
    record NodeLeft(String nodeId) implements ClusterEvent {}
    record LeaderElected(String leaderId) implements ClusterEvent {}
    record JobDistributed(String jobId, String targetNode) implements ClusterEvent {}
    record Heartbeat(String nodeId, long timestamp) implements ClusterEvent {}
}