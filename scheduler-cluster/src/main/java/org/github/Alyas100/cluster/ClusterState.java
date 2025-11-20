package org.github.Alyas100.cluster;

/**
 * Immutable snapshot of cluster state.
 */
public record ClusterState(String leaderId, int totalNodes, int activeNodes, boolean healthy) {

    public ClusterState {
        // Validation
        if (totalNodes < 0) throw new IllegalArgumentException("totalNodes cannot be negative");
        if (activeNodes < 0 || activeNodes > totalNodes) {
            throw new IllegalArgumentException("activeNodes must be between 0 and totalNodes");
        }
    }

    public int getInactiveNodes() {
        return totalNodes - activeNodes;
    }

    public boolean hasQuorum() {
        return activeNodes > (totalNodes / 2);
    }

    public static ClusterState empty() {
        return new ClusterState("none", 0, 0, false);
    }
}