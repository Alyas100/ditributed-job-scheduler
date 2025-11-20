package org.github.Alyas100.storage;

import org.github.Alyas100.JobExecutionResult;
import java.time.Instant;
import java.util.List;

public interface JobExecutionStore {
    void saveExecution(String jobId, JobExecutionResult result, Instant executedAt);
    List<ExecutionRecord> getExecutionHistory(String jobId, int limit);

    record ExecutionRecord(String jobId, JobExecutionResult result, Instant executedAt) {}
}