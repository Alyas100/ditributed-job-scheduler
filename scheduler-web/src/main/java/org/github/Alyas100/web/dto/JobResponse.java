package org.github.Alyas100.web.dto;

import org.github.Alyas100.JobDefinition;
import org.github.Alyas100.RetryPolicy;

import java.time.Duration;
import java.util.Map;

public record JobResponse(
        String jobId,
        String jobName,
        String pluginName,
        String cronExpression,
        Map<String, Object> parameters,
        RetryPolicy retryPolicy,
        Duration timeout,
        int maxRetries,
        boolean enabled,
        String status
) {
    public static JobResponse fromJobDefinition(JobDefinition job) {
        return new JobResponse(
                job.jobId(),
                job.jobName(),
                job.pluginName(),
                job.cronExpression(),
                job.parameters(),
                job.retryPolicy(),
                job.timeout(),
                job.maxRetries(),
                job.enabled(),
                "SCHEDULED"  // Simple status for demo
        );
    }
}