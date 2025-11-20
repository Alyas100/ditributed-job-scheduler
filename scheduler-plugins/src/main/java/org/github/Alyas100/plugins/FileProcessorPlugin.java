package org.github.Alyas100.plugins;

import org.github.Alyas100.*;
import org.github.Alyas100.annotation.ScheduledJob;

import java.time.Duration;
import java.util.Map;

@ScheduledJob(
        name = "file-processor",
        description = "Reads a file and counts lines",
        version = "1.0"
)
public class FileProcessorPlugin implements JobPlugin {

    @Override
    public String getPluginName() { return "file-processor"; }

    @Override
    public String getVersion() { return "1.0"; }

    @Override
    public JobExecutionResult execute(JobExecutionContext context) {
        // Your logic to read file, count lines, etc.
        return new JobExecutionResult.Success(
                "File processed",
                Map.of("lines", 42),
                Duration.ofMillis(500),
                0L
        );
    }

    // will be used by class 'pluginregistry'
    @Override
    public void initialize(PluginConfiguration config) { }

    @Override
    public void shutdown() { }

    @Override
    public boolean isHealthy() { return true; }
}
