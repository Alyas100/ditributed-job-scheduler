package org.github.Alyas100.core;

import org.github.Alyas100.JobDefinition;
import org.github.Alyas100.core.CronExpression;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manages scheduled job execution using cron expressions.
 */
public class JobScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final JobExecutor jobExecutor;
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();

    public JobScheduler(JobExecutor jobExecutor) {
        this.jobExecutor = jobExecutor;
    }

    /**
     * Schedules a job based on its cron expression.
     */
    public void scheduleJob(JobDefinition job) {
        if (job.cronExpression() == null || job.cronExpression().isBlank()) {
            throw new IllegalArgumentException("Job must have a cron expression: " + job.jobId());
        }

        CronExpression cron = CronExpressionParser.parse(job.cronExpression());
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> jobExecutor.executeJob(Optional.of(job)),
                computeInitialDelay(cron),
                Duration.ofMinutes(1).toMillis(), // Fixed rate for simplicity
                TimeUnit.MILLISECONDS
        );

        scheduledJobs.put(job.jobId(), future);
    }

    /**
     * Computes initial delay until next cron execution.
     */
    private long computeInitialDelay(CronExpression cron) {
        LocalDateTime nextExecution = cron.next(LocalDateTime.now());
        return Duration.between(LocalDateTime.now(), nextExecution).toMillis();
    }

    public void unscheduleJob(String jobId) {
        ScheduledFuture<?> future = scheduledJobs.remove(jobId);
        if (future != null) {
            future.cancel(false);
        }
    }

    public void shutdown() {
        scheduledJobs.values().forEach(future -> future.cancel(false));
        scheduler.shutdown();
    }
}