package org.github.Alyas100.core;

import org.github.Alyas100.*;

import java.util.Optional;
import java.util.concurrent.*;

/**
 * Handles actual job execution with timeout and exception handling.
 */
public class JobExecutor {
    private final PluginRegistry pluginRegistry;
    private final ExecutorService executorService;

    public JobExecutor(PluginRegistry pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Executes a job asynchronously with timeout support.
     */
    public CompletableFuture<JobExecutionResult> executeJob(Optional<JobDefinition> jobOpt) {
        return CompletableFuture.supplyAsync(() -> {
            JobDefinition job = jobOpt.orElseThrow(() -> new IllegalArgumentException("Job is missing")); // check first if the 'JobDefinition type object' exist in Optional, if yes extract it from 'Optional' and assigns it into 'job', else throw excp
            JobPlugin plugin = pluginRegistry.getPlugin(job.pluginName(), job.parameters());
            JobExecutionContext context = new JobExecutionContextImpl(job);

            try {
                return plugin.execute(context);
            } catch (Exception e) {
                return new JobExecutionResult.Failure(
                        "Job execution failed: " + e.getMessage(),
                        e,
                        java.time.Duration.ZERO,
                        true,
                        ErrorType.PLUGIN_ERROR,
                        0
                );
            }
        }, executorService);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}