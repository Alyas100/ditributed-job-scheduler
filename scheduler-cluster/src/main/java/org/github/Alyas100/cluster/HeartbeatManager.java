package org.github.Alyas100.cluster;

import java.util.Set;

/**
 * Manages node heartbeats and failure detection.
 */
public interface HeartbeatManager {
    void startHeartbeat();
    void stopHeartbeat();
    void sendHeartbeat();
    boolean isNodeAlive(String nodeId);
    Set<String> getDeadNodes();
    long getTimeSinceLastHeartbeat(String nodeId);
}