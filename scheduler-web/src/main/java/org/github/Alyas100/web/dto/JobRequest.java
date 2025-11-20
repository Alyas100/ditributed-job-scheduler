package org.github.Alyas100.web.dto;

import org.github.Alyas100.JobDefinition;
import org.github.Alyas100.RetryPolicy;

import java.time.Duration;
import java.util.Map;

public record JobRequest(
        String jobId,
        String jobName,
        String pluginName,
        String cronExpression,
        Map<String, Object> parameters,
        RetryPolicyRequest retryPolicy,
        Duration timeout,
        int maxRetries,
        boolean enabled
) {
    public JobDefinition toJobDefinition() {
        return new JobDefinition(
                jobId,
                jobName,
                pluginName,
                cronExpression,
                parameters,
                retryPolicy != null ? retryPolicy.toRetryPolicy() : new RetryPolicy(
                        Duration.ofSeconds(30), Duration.ofMinutes(5), 2.0, true
                ),
                timeout != null ? timeout : Duration.ofMinutes(5),
                maxRetries > 0 ? maxRetries : 3,
                enabled
        );
    }

    public record RetryPolicyRequest(
            Duration initialDelay,
            Duration maxDelay,
    double backoffMultiplier,
    boolean exponentialBackoff
    ) {
        public RetryPolicy toRetryPolicy() {
            return new RetryPolicy(
                    initialDelay != null ? initialDelay : Duration.ofSeconds(30),
                    maxDelay != null ? maxDelay : Duration.ofMinutes(5),
                    backoffMultiplier > 0 ? backoffMultiplier : 2.0,
                    exponentialBackoff
            );
        }
    }
}