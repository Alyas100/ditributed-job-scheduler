package org.github.Alyas100.core;

import org.github.Alyas100.JobDefinition;
import org.github.Alyas100.JobExecutionResult;
import org.github.Alyas100.cluster.ClusterManager;
import org.github.Alyas100.cluster.ClusterListener;  // ✅ YOUR ClusterListener
import org.github.Alyas100.cluster.ClusterNode;      // ✅ YOUR ClusterNode
import org.github.Alyas100.cluster.ClusterState;     // ✅ YOUR ClusterState (NOT Hazelcast's!)
import org.github.Alyas100.storage.JobRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main orchestrator that ties everything together.
 * This is the primary API for interacting with the scheduler.
 */
public class SchedulerEngine {
    private final PluginRegistry pluginRegistry;
    private final JobExecutor jobExecutor;
    private final JobScheduler jobScheduler;
    private final List<SchedulerListener> listeners = new CopyOnWriteArrayList<>();
    private final JobRepository jobRepository;
    private final ClusterManager clusterManager;
    private final Map<String, JobDefinition> jobDefinitions = new ConcurrentHashMap<>(); // ✅ ADD THIS!

    private volatile boolean running = false;

    /**
     * @param pluginBasePackage the root package name where scheduler should look for plugins when scanning the classpath
     * @param jobRepository storage layer used for persisting job definitions
     * @param clusterManager cluster coordination manager (can be null for single-node)
     */
    public SchedulerEngine(String pluginBasePackage, JobRepository jobRepository, ClusterManager clusterManager) {
        this.pluginRegistry = new PluginRegistry(pluginBasePackage);
        this.jobExecutor = new JobExecutor(pluginRegistry);
        this.jobScheduler = new JobScheduler(jobExecutor);
        this.jobRepository = jobRepository;
        this.clusterManager = clusterManager;

        setupClusterListeners();
        loadPersistedJobs(); // ✅ CALL THIS!
    }

    /**
     * Constructor without clustering (backward compatibility)
     */
    public SchedulerEngine(String pluginBasePackage, JobRepository jobRepository) {
        this(pluginBasePackage, jobRepository, null);
    }

    /**
     * Constructor without storage or clustering (legacy)
     */
    public SchedulerEngine(String pluginBasePackage) {
        this(pluginBasePackage, null, null);
    }

    /**
     * Loads persisted jobs from storage on startup.
     */
    private void loadPersistedJobs() {  // ✅ IMPLEMENT THIS!
        if (jobRepository == null) {
            System.out.println("💡 No persistence configured - skipping job loading");
            return;
        }

        try {
            List<JobDefinition> persistedJobs = jobRepository.getAllJobs();
            System.out.println("📂 Found " + persistedJobs.size() + " persisted jobs in storage");

            int scheduledCount = 0;
            for (JobDefinition job : persistedJobs) {
                jobDefinitions.put(job.jobId(), job);

                // Only schedule if we're the leader (in cluster) or always (single-node)
                boolean shouldSchedule = clusterManager == null || clusterManager.isLeader();
                if (shouldSchedule && job.enabled() && job.cronExpression() != null && !job.cronExpression().isBlank()) {
                    jobScheduler.scheduleJob(job);
                    scheduledCount++;
                }
            }

            System.out.println("✅ Loaded and scheduled " + scheduledCount + " persisted jobs");

        } catch (Exception e) {
            System.out.println("❌ Failed to load persisted jobs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets up cluster event listeners.
     */
    private void setupClusterListeners() {
        if (clusterManager == null) {
            return;
        }

        clusterManager.addClusterListener(new ClusterListener() {
            @Override
            public void onLeaderElected(String leaderId) {
                System.out.println("👑 New cluster leader: " + leaderId);
                if (clusterManager.isLeader()) {
                    System.out.println("🎯 I am the new leader! Taking over job scheduling...");
                    scheduleAllEnabledJobs(); // ✅ Schedule all jobs as new leader
                } else {
                    System.out.println("💼 I am a worker node. Following leader: " + leaderId);
                    unscheduleAllJobs(); // ✅ Unschedule all jobs as worker
                }
            }

            @Override
            public void onNodeJoined(ClusterNode node) {
                System.out.println("🟢 Node joined: " + node.getNodeId());
            }

            @Override
            public void onNodeLeft(ClusterNode node) {
                System.out.println("🔴 Node left: " + node.getNodeId());
                if (clusterManager.isLeader()) {
                    clusterManager.rebalanceJobs();
                }
            }
        });
    }

    /**
     * Schedule all enabled jobs (called when becoming leader)
     */
    private void scheduleAllEnabledJobs() {
        int scheduled = 0;
        for (JobDefinition job : jobDefinitions.values()) {
            if (job.enabled() && job.cronExpression() != null && !job.cronExpression().isBlank()) {
                jobScheduler.scheduleJob(job);
                scheduled++;
            }
        }
        System.out.println("📅 Leader scheduled " + scheduled + " enabled jobs");
    }

    /**
     * Unschedule all jobs (called when becoming worker)
     */
    private void unscheduleAllJobs() {
        for (String jobId : jobDefinitions.keySet()) {
            jobScheduler.unscheduleJob(jobId);
        }
        System.out.println("⏸️  Worker unscheduled all jobs");
    }

    /**
     * Starts the scheduler engine.
     */
    public void start() {
        if (running) {
            throw new IllegalStateException("Scheduler is already running");
        }

        running = true;

        // Start cluster if available
        if (clusterManager != null) {
            clusterManager.start();
        }

        listeners.forEach(SchedulerListener::onSchedulerStart);
        System.out.println("🚀 Scheduler Engine Started" +
                (clusterManager != null ? " with CLUSTERING" : "") +
                (jobRepository != null ? " with PERSISTENCE" : ""));
    }

    /**
     * Stops the scheduler engine gracefully.
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        // Stop cluster if available
        if (clusterManager != null) {
            clusterManager.stop();
        }

        jobScheduler.shutdown();
        jobExecutor.shutdown();
        pluginRegistry.shutdown();
        listeners.forEach(SchedulerListener::onSchedulerStop);
        System.out.println("🛑 Scheduler Engine Stopped");
    }

    /**
     * Registers a job definition with the scheduler. ✅ SINGLE VERSION!
     */
    public void scheduleJob(JobDefinition job) {
        validateJobDefinition(job);

        // Persist to storage if available
        if (jobRepository != null) {
            jobRepository.saveJob(job);
        }

        jobDefinitions.put(job.jobId(), job);

        // Only schedule if we're the leader (in cluster) or always (single-node)
        boolean shouldSchedule = clusterManager == null || clusterManager.isLeader();
        if (shouldSchedule && job.cronExpression() != null && !job.cronExpression().isBlank()) {
            jobScheduler.scheduleJob(job);
        }

        // Distribute job info to cluster
        if (clusterManager != null) {
            clusterManager.distributeJob(job);
        }

        listeners.forEach(listener -> listener.onJobScheduled(job));
    }

    /**
     * Executes a job immediately (on-demand).
     */
    public CompletableFuture<JobExecutionResult> executeJobNow(String jobId) {
        JobDefinition job = jobDefinitions.get(jobId); // ✅ Use local cache
        if (job == null) {
            throw new IllegalArgumentException("Job not found: " + jobId);
        }

        listeners.forEach(listener -> listener.onJobExecuting(Optional.of(job)));
        return jobExecutor.executeJob(Optional.of(job));
    }

    /**
     * Removes a job from the scheduler.
     */
    public void unscheduleJob(String jobId) {
        JobDefinition job = jobDefinitions.remove(jobId);
        if (job != null) {
            // Remove from storage if available
            if (jobRepository != null) {
                jobRepository.deleteJob(jobId);
            }

            jobScheduler.unscheduleJob(jobId);
            listeners.forEach(listener -> listener.onJobUnscheduled(job));
        }
    }

    /**
     * Gets all registered job definitions.
     */
    public Collection<JobDefinition> getScheduledJobs() {
        return Collections.unmodifiableCollection(jobDefinitions.values());
    }

    /**
     * Gets available plugins discovered by the registry.
     */
    public Set<String> getAvailablePlugins() {
        return pluginRegistry.getAvailablePlugins();
    }

    /**
     * Adds a listener for scheduler events.
     */
    public void addListener(SchedulerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     */
    public void removeListener(SchedulerListener listener) {
        listeners.remove(listener);
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Gets cluster state if clustering is enabled.
     */
    public ClusterState getClusterState() {
        return clusterManager != null ? clusterManager.getClusterState() : null;
    }

    private void validateJobDefinition(JobDefinition job) {
        if (job.jobId() == null || job.jobId().isBlank()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }
        if (job.pluginName() == null || job.pluginName().isBlank()) {
            throw new IllegalArgumentException("Plugin name cannot be null or empty");
        }
        if (!pluginRegistry.getAvailablePlugins().contains(job.pluginName())) {
            throw new IllegalArgumentException("Unknown plugin: " + job.pluginName());
        }
    }
}