package org.github.Alyas100.cluster;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a node in the distributed scheduler cluster.
 * Each node can be a leader (schedules jobs) or worker (executes jobs).
 */
public class ClusterNode {
    private final String nodeId;
    private final String host;
    private final int port;
    private final long startupTime;
    private volatile long lastHeartbeat;
    private volatile NodeStatus status;
    private volatile int cpuLoad; // 0-100 percentage
    private volatile long memoryUsed; // in MB
    private volatile int activeJobs;

    public ClusterNode(String nodeId, String host, int port) {
        this.nodeId = Objects.requireNonNull(nodeId, "Node ID cannot be null");
        this.host = Objects.requireNonNull(host, "Host cannot be null");
        this.port = port;
        this.startupTime = System.currentTimeMillis();
        this.lastHeartbeat = System.currentTimeMillis();
        this.status = NodeStatus.ACTIVE;
        this.cpuLoad = 0;
        this.memoryUsed = 0;
        this.activeJobs = 0;
    }

    // Getters
    public String getNodeId() { return nodeId; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public long getStartupTime() { return startupTime; }
    public long getLastHeartbeat() { return lastHeartbeat; }
    public NodeStatus getStatus() { return status; }
    public int getCpuLoad() { return cpuLoad; }
    public long getMemoryUsed() { return memoryUsed; }
    public int getActiveJobs() { return activeJobs; }

    // Setters for mutable state
    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public void setCpuLoad(int cpuLoad) {
        this.cpuLoad = Math.max(0, Math.min(100, cpuLoad));
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = Math.max(0, memoryUsed);
    }

    public void setActiveJobs(int activeJobs) {
        this.activeJobs = Math.max(0, activeJobs);
    }

    public void incrementActiveJobs() {
        this.activeJobs++;
    }

    public void decrementActiveJobs() {
        this.activeJobs = Math.max(0, this.activeJobs - 1);
    }

    // Utility methods
    public boolean isActive() {
        return status == NodeStatus.ACTIVE;
    }

    public boolean isSuspected() {
        return status == NodeStatus.SUSPECTED;
    }

    public boolean isDown() {
        return status == NodeStatus.DOWN;
    }

    public long getUptime() {
        return System.currentTimeMillis() - startupTime;
    }

    public long getTimeSinceLastHeartbeat() {
        return System.currentTimeMillis() - lastHeartbeat;
    }

    public boolean isHeartbeatStale(long timeoutMs) {
        return getTimeSinceLastHeartbeat() > timeoutMs;
    }

    public double getLoadScore() {
        // Calculate a load score for job distribution (lower is better)
        return (cpuLoad * 0.5) + (activeJobs * 0.3) + (memoryUsed / 1024.0 * 0.2);
    }

    // Node status enumeration
    public enum NodeStatus {
        ACTIVE("Active"),           // Node is healthy and responsive
        SUSPECTED("Suspected"),     // Node might be having issues
        DOWN("Down"),               // Node is not responding
        SHUTTING_DOWN("Shutting Down"); // Node is gracefully shutting down

        private final String displayName;

        NodeStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterNode that = (ClusterNode) o;
        return Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        return String.format(
                "ClusterNode{id=%s, host=%s:%d, status=%s, load=%d%%, jobs=%d, uptime=%ds}",
                nodeId.substring(0, 8) + "...",
                host,
                port,
                status,
                cpuLoad,
                activeJobs,
                getUptime() / 1000
        );
    }

    // Builder pattern for fluent creation
    public static class Builder {
        private String nodeId;
        private String host;
        private int port = 5701; // Default Hazelcast port

        public Builder withNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public ClusterNode build() {
            return new ClusterNode(nodeId, host, port);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Static factory methods
    public static ClusterNode createLocalNode() {
        String host = getLocalHost();
        String nodeId = generateNodeId(host);
        return new ClusterNode(nodeId, host, 5701);
    }

    public static ClusterNode createNode(String host, int port) {
        String nodeId = generateNodeId(host);
        return new ClusterNode(nodeId, host, port);
    }

    private static String getLocalHost() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }

    private static String generateNodeId(String host) {
        return host + "-" + Instant.now().toEpochMilli() + "-" + Math.abs(new java.util.Random().nextInt(10000));
    }
}