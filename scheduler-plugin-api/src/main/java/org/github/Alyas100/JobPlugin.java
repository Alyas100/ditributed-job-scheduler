package org.github.Alyas100;

import java.util.Map;

// The job plugin interface
public interface JobPlugin {
    /** Unique name for this plugin type */
    String getPluginName();

    /** Version of the plugin */
    String getVersion();

    /** Execute the job with given context */
    JobExecutionResult execute(JobExecutionContext context)
            throws JobExecutionException;

    /** Initialize plugin with configuration.
     * For lifecycle management */
    void initialize(PluginConfiguration config);

    /** Graceful shutdown */
    void shutdown();

    /** Health check */
    boolean isHealthy();
}
