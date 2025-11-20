package org.github.Alyas100.core;

import org.github.Alyas100.JobDefinition;
import org.github.Alyas100.JobExecutionContext;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JobExecutionContextImpl implements JobExecutionContext {
    private final JobDefinition jobDefinition;
    private final Instant executionTime;
    private final Map<String, Object> sharedState = new ConcurrentHashMap<>();

    public JobExecutionContextImpl(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
        this.executionTime = Instant.now();
    }

    @Override public String getJobId() { return jobDefinition.jobId(); }
    @Override public String getJobName() { return jobDefinition.jobName(); }
    @Override public String getPluginName() { return jobDefinition.pluginName(); }
    @Override public Instant getScheduledTime() { return executionTime; }
    @Override public Instant getActualExecutionTime() { return Instant.now(); }
    @Override public Instant getJobCreationTime() { return Instant.now(); }
    @Override public Map<String, Object> getParameters() { return jobDefinition.parameters(); }
    @Override public String getNodeId() { return "local-node"; }
    @Override public String getCorrelationId() { return jobDefinition.jobId() + "-" + System.currentTimeMillis(); }
    @Override public boolean isRecoveryExecution() { return false; }

    @Override
    public void updateProgress(int percentage, String statusMessage) {
        System.out.println("📊 [" + jobDefinition.jobId() + "] Progress: " + percentage + "% - " + statusMessage);
    }

    @Override
    public void addMetric(String name, Object value) {
        System.out.println("📈 [" + jobDefinition.jobId() + "] Metric: " + name + " = " + value);
    }

    @Override
    public void info(String message) {
        System.out.println("ℹ️ [" + jobDefinition.jobId() + "] " + message);
    }

    @Override
    public void warn(String message) {
        System.out.println("⚠️ [" + jobDefinition.jobId() + "] " + message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        System.out.println("❌ [" + jobDefinition.jobId() + "] " + message);
        if (throwable != null) throwable.printStackTrace();
    }

    @Override
    public void putSharedState(String key, Object value) {
        sharedState.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSharedState(String key, Class<T> type) {
        return (T) sharedState.get(key);
    }

    @Override public boolean isLeaderNode() { return true; }
    @Override public int getTotalClusterNodes() { return 1; }
}