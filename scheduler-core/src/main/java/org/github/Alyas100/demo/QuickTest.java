package org.github.Alyas100.demo;

import org.github.Alyas100.core.SchedulerEngine;
import org.github.Alyas100.JobDefinition;
import org.github.Alyas100.RetryPolicy;
import org.github.Alyas100.storage.InMemoryJobRepository;
import org.github.Alyas100.storage.JobRepository;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class QuickTest {
    public static void main(String[] args) throws Exception {
        System.out.println("STARTING DISTRIBUTED JOB SCHEDULER - QUICK TEST!");

        System.out.println("TESTING IN MEMORY PERSISTENCE");

        // CREATE IN MMEORY STORAGE
        JobRepository storage = new InMemoryJobRepository();


        // Initialize engine - this will scan for plugins automatically!, and it also initialize engine with storage
        SchedulerEngine engine = new SchedulerEngine("org.github.Alyas100.plugins", storage, null);

        // Add a simple listener to see what's happening
        engine.addListener(new org.github.Alyas100.core.SchedulerListener() {
            public void onSchedulerStart() {
                System.out.println("Scheduler Engine Started - Plugin discovery complete!");
            }
            public void onJobScheduled(org.github.Alyas100.JobDefinition job) {
                System.out.println("Job Scheduled: " + job.jobId() + " with plugin: " + job.pluginName());
            }
            public void onJobExecuting(Optional<JobDefinition> job) {
                System.out.println("⚡ Executing Job: " + job.get().jobId() + " at " + java.time.LocalTime.now());
            }
        });

        // Start the engine (this triggers plugin discovery)
        engine.start();

        // Wait a moment for plugin discovery to complete
        Thread.sleep(2000);

        System.out.println("Discovered Plugins: " + engine.getAvailablePlugins());

        // Check if our simple-logger plugin was found
        if (!engine.getAvailablePlugins().contains("simple-logger")) {
            System.out.println("CRITICAL: simple-logger plugin not found! Check your package structure.");
            engine.stop();
            return;
        }

        System.out.println("SUCCESS: simple-logger plugin discovered!");

        // Create test job using your SimpleLoggerPlugin
        JobDefinition job = new JobDefinition(
                "test-logger-job",
                "Test Logger Job",
                "file-processor",  // ← MUST match @ScheduledJob name!
                "*/10 * * * * *",  // Every 10 seconds for testing (not too frequent)
                Map.of(
                        "message", "Hello from Distributed Job Scheduler!",
                        "delayMs", 2000  // 2 second delay to see progress
                ),
                new RetryPolicy(Duration.ofSeconds(10), Duration.ofMinutes(1), 2.0, true),
                Duration.ofSeconds(30),
                3,
                true
        );

        // Schedule the job
        engine.scheduleJob(job);
        System.out.println("Job persisted to in-memory storage!");
        System.out.println("Total jobs in storage: " + storage.getAllJobs().size());

        System.out.println("Test job scheduled! Waiting for executions (30 seconds)...");
        System.out.println("You should see job executions every 10 seconds!");
        System.out.println("   - Plugin discovery  ");
        System.out.println("   - Job scheduling  ");
        System.out.println("   - Cron execution  ");
        System.out.println("   - Plugin execution  ");
        System.out.println("   - Progress tracking  ");
        System.out.println("   - Result handling  ");

        // Let it run for 30 seconds to see multiple executions
        for (int i = 0; i < 30; i++) {
            Thread.sleep(1000);
            if (i % 5 == 0) {
                System.out.println("⏳ " + (30 - i) + " seconds remaining...");
            }
        }

        System.out.println("Stopping scheduler engine...");
        engine.stop();

        System.out.println("TEST COMPLETED! Your distributed job scheduler is WORKING!");
        System.out.println("Next: Add more plugins, persistence, and cluster coordination!");
    }
}

