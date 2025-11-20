package org.github.Alyas100;

/**
 * Checked exception hierarchy for job execution failures.
 */
public abstract class JobExecutionException extends Exception {
    public JobExecutionException(String message) { super(message); }
    public JobExecutionException(String message, Throwable cause) { super(message, cause); }
}

// Specific exception types
final class PluginConfigurationException extends JobExecutionException {
    public PluginConfigurationException(String message) { super(message); }
}

final class PluginExecutionException extends JobExecutionException {
    public PluginExecutionException(String message, Throwable cause) { super(message, cause); }
}

final class PluginTimeoutException extends JobExecutionException {
    public PluginTimeoutException(String message) { super(message); }
}

final class PluginResourceException extends JobExecutionException {
    public PluginResourceException(String message) { super(message); }
}