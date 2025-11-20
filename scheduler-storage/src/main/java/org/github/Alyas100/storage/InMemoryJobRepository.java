package org.github.Alyas100.storage;

import org.github.Alyas100.JobDefinition;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryJobRepository implements JobRepository {
    private final Map<String, JobDefinition> jobs = new ConcurrentHashMap<>();

    @Override
    public void saveJob(JobDefinition job) {
        jobs.put(job.jobId(), job);
    }

    @Override
    public Optional<JobDefinition> getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    @Override
    public List<JobDefinition> getAllJobs() {
        return new ArrayList<>(jobs.values());
    }

    @Override
    public JobDefinition deleteJob(String jobId) {
        return jobs.remove(jobId); // user can see or acces the removed job
    }

    @Override
    public boolean jobExists(String jobId) {
        return jobs.containsKey(jobId);
    }
}