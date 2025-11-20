package org.github.Alyas100;

import java.time.Duration;
import java.util.Map;

/**
 * Complete job definition that can be scheduled and persisted.
 */
public record JobDefinition(
        String jobId,
        String jobName,
        String pluginName,
        String cronExpression,  // For scheduled jobs
        Map<String, Object> parameters,
        RetryPolicy retryPolicy,
        Duration timeout,
        int maxRetries,
        boolean enabled
) {
    public JobDefinition {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("Job ID cannot be null or blank");
        }
        if (pluginName == null || pluginName.isBlank()) {
            throw new IllegalArgumentException("Plugin name cannot be null or blank");
        }
    }
}

