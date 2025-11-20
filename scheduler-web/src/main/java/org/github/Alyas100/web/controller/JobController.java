package org.github.Alyas100.web.controller;

import org.github.Alyas100.core.SchedulerEngine;
import org.github.Alyas100.JobDefinition;
import org.github.Alyas100.JobExecutionResult;
import org.github.Alyas100.web.dto.JobRequest;
import org.github.Alyas100.web.dto.JobResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")  // Allow frontend access
public class JobController {

    private final SchedulerEngine schedulerEngine;

    public JobController(SchedulerEngine schedulerEngine) {
        this.schedulerEngine = schedulerEngine;
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        List<JobResponse> jobs = schedulerEngine.getScheduledJobs().stream()
                .map(JobResponse::fromJobDefinition)
                .collect(Collectors.toList());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable String jobId) {
        return schedulerEngine.getScheduledJobs().stream()
                .filter(job -> job.jobId().equals(jobId))
                .findFirst()
                .map(JobResponse::fromJobDefinition)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest jobRequest) {
        JobDefinition jobDefinition = jobRequest.toJobDefinition();
        schedulerEngine.scheduleJob(jobDefinition);
        return ResponseEntity.ok(JobResponse.fromJobDefinition(jobDefinition));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable String jobId, @RequestBody JobRequest jobRequest) {
        // For simplicity, delete and recreate
        schedulerEngine.unscheduleJob(jobId);
        JobDefinition updatedJob = jobRequest.toJobDefinition();
        schedulerEngine.scheduleJob(updatedJob);
        return ResponseEntity.ok(JobResponse.fromJobDefinition(updatedJob));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable String jobId) {
        schedulerEngine.unscheduleJob(jobId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{jobId}/execute")
    public ResponseEntity<String> executeJobNow(@PathVariable String jobId) {
        try {
            CompletableFuture<JobExecutionResult> future = schedulerEngine.executeJobNow(jobId);
            return ResponseEntity.accepted().body("Job execution started: " + jobId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to execute job: " + e.getMessage());
        }
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<String> getJobStatus(@PathVariable String jobId) {
        // Simple status check - in production, you'd track execution status
        boolean exists = schedulerEngine.getScheduledJobs().stream()
                .anyMatch(job -> job.jobId().equals(jobId));
        return exists ? ResponseEntity.ok("EXISTS") : ResponseEntity.notFound().build();
    }
}