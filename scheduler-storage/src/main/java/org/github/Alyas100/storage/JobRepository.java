package org.github.Alyas100.storage;

import org.github.Alyas100.JobDefinition;
import java.util.List;
import java.util.Optional;

public interface JobRepository {
    void saveJob(JobDefinition job);
    Optional<JobDefinition> getJob(String jobId);
    List<JobDefinition> getAllJobs();
    JobDefinition deleteJob(String jobId);
    boolean jobExists(String jobId);
}