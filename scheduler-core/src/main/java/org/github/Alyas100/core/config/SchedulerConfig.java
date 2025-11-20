package org.github.Alyas100.core.config;

import java.time.Duration;
import java.util.Properties;

/**
 * Configuration for the scheduler engine.
 * Loads settings from properties files, environment variables, or code.
 */
public class SchedulerConfig {
    private final Properties properties;

    // Default values
    private static final String DEFAULT_PLUGIN_PACKAGE = "org.github.Alyas100.plugins";
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;
    private static final Duration DEFAULT_JOB_TIMEOUT = Duration.ofMinutes(30);
    private static final boolean DEFAULT_CLUSTER_ENABLED = false;

    public SchedulerConfig() {
        this.properties = loadDefaultProperties();
    }

    public SchedulerConfig(Properties properties) {
        this.properties = new Properties(loadDefaultProperties());
        this.properties.putAll(properties);  // overrides any defaults with the values in the 'properties argument'
    }

    private Properties loadDefaultProperties() {
        Properties defaults = new Properties();
        defaults.setProperty("scheduler.plugin.base.package", DEFAULT_PLUGIN_PACKAGE);
        defaults.setProperty("scheduler.thread.pool.size", String.valueOf(DEFAULT_THREAD_POOL_SIZE));
        defaults.setProperty("scheduler.job.timeout.seconds",
                String.valueOf(DEFAULT_JOB_TIMEOUT.getSeconds()));
        defaults.setProperty("scheduler.cluster.enabled", String.valueOf(DEFAULT_CLUSTER_ENABLED));
        defaults.setProperty("scheduler.metrics.enabled", "true");
        defaults.setProperty("scheduler.persistence.enabled", "false");
        return defaults;
    }

    // Getters with type conversion and fallbacks

    public String getPluginBasePackage() {
        return properties.getProperty("scheduler.plugin.base.package", DEFAULT_PLUGIN_PACKAGE);
    }

    public int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("scheduler.thread.pool.size",
                String.valueOf(DEFAULT_THREAD_POOL_SIZE)));
    }

    public Duration getJobTimeout() {
        long seconds = Long.parseLong(properties.getProperty("scheduler.job.timeout.seconds",
                String.valueOf(DEFAULT_JOB_TIMEOUT.getSeconds())));
        return Duration.ofSeconds(seconds);
    }

    public boolean isClusterEnabled() {
        return Boolean.parseBoolean(properties.getProperty("scheduler.cluster.enabled",
                String.valueOf(DEFAULT_CLUSTER_ENABLED)));
    }

    public boolean isMetricsEnabled() {
        return Boolean.parseBoolean(properties.getProperty("scheduler.metrics.enabled", "true"));
    }

    public boolean isPersistenceEnabled() {
        return Boolean.parseBoolean(properties.getProperty("scheduler.persistence.enabled", "false"));
    }

    // Builder pattern for fluent configuration
    public static class Builder {
        private final Properties properties = new Properties();

        public Builder withPluginBasePackage(String packageName) {
            properties.setProperty("scheduler.plugin.base.package", packageName);
            return this;
        }

        public Builder withThreadPoolSize(int size) {
            properties.setProperty("scheduler.thread.pool.size", String.valueOf(size));
            return this;
        }

        public Builder withJobTimeout(Duration timeout) {
            properties.setProperty("scheduler.job.timeout.seconds",
                    String.valueOf(timeout.getSeconds()));
            return this;
        }

        public Builder withClusterEnabled(boolean enabled) {
            properties.setProperty("scheduler.cluster.enabled", String.valueOf(enabled));
            return this;
        }

        public Builder withMetricsEnabled(boolean enabled) {
            properties.setProperty("scheduler.metrics.enabled", String.valueOf(enabled));
            return this;
        }

        public Builder withPersistenceEnabled(boolean enabled) {
            properties.setProperty("scheduler.persistence.enabled", String.valueOf(enabled));
            return this;
        }

        public SchedulerConfig build() {
            return new SchedulerConfig(properties);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}