package org.github.Alyas100.plugins;

import org.github.Alyas100.*;
import org.github.Alyas100.annotation.ScheduledJob;

import java.time.Duration;
import java.util.Map;

@ScheduledJob(
        name = "simple-logger",
        description = "Simple logging plugin for testing",
        version = "1.0"
)
public class SimpleLoggerPlugin implements JobPlugin {

    @Override
    public String getPluginName() {
        return "simple-logger";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public JobExecutionResult execute(JobExecutionContext context) {
        try {
            String message = (String) context.getParameters().get("message");
            int delay = (Integer) context.getParameters().getOrDefault("delayMs", 1000);

            context.info("SimpleLoggerPlugin starting execution...");
            context.updateProgress(25, "Processing message");

            // Simulate work
            Thread.sleep(delay);

            context.updateProgress(100, "Completed successfully");
            context.info("Logged message: " + message);

            return new JobExecutionResult.Success(
                    "Message logged successfully: " + message,
                    Map.of("loggedAt", System.currentTimeMillis(), "messageLength", message.length()),
                    Duration.ofMillis(delay),
                    0L
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new JobExecutionResult.Failure(
                    "Job interrupted",
                    e,
                    Duration.ZERO,
                    true,
                    ErrorType.PLUGIN_ERROR,
                    0
            );
        } catch (Exception e) {
            return new JobExecutionResult.Failure(
                    "Failed to log message: " + e.getMessage(),
                    e,
                    Duration.ZERO,
                    true,
                    ErrorType.PLUGIN_ERROR,
                    0
            );
        }
    }

    @Override
    public void initialize(PluginConfiguration config) {
        System.out.println("SimpleLoggerPlugin initialized with config: " + config.toMap());
    }

    @Override
    public void shutdown() {
        System.out.println("SimpleLoggerPlugin shutting down...");
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}