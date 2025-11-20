package org.github.Alyas100;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable context provided to plugins during job execution.
 * Contains everything a plugin needs to do its job in a distributed environment.
 *
 * <p>This context is serialized and passed across cluster nodes, so keep it lean.</p>
 */

    /*
    Each job execution gets its own snapshot of contextual data.
    plugins cannot modify the job's metadata like job ID, times ,etc. they just can read it.
It ensures thread-safety and consistency, especially in a distributed setup (multiple threads/nodes running jobs at once).
     */
public interface JobExecutionContext {

    // Core Job Identity
    String getJobId();
    String getJobName();
    String getPluginName();

    // Timing Information which is critical for distributed systems
    Instant getScheduledTime();
    Instant getActualExecutionTime();
    Instant getJobCreationTime();

    // Job Configuration & Parameters
    Map<String, Object> getParameters();
    String getNodeId();  // Which cluster node is executing this

    // Distributed Coordination
    String getCorrelationId();  // For tracing across cluster
    boolean isRecoveryExecution();  // Is this a retry after failure?

    // Progress Tracking & Monitoring
    void updateProgress(int percentage, String statusMessage);
    void addMetric(String name, Object value);

    // Distributed Logging (goes to central log aggregation)
    void info(String message);
    void warn(String message);
    void error(String message, Throwable throwable);

    // State Sharing (for multi-step jobs)
    void putSharedState(String key, Object value);
    <T> T getSharedState(String key, Class<T> type);

    // Cluster Awareness
    boolean isLeaderNode();
    int getTotalClusterNodes();
}