package org.github.Alyas100.web;

import org.github.Alyas100.core.SchedulerEngine;
import org.github.Alyas100.storage.JobRepository;
import org.github.Alyas100.cluster.ClusterManager;
import org.github.Alyas100.cluster.hazelcast.HazelcastClusterManager;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class SchedulerWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerWebApplication.class, args);
    }

    @Bean
    public SchedulerEngine schedulerEngine(JobRepository jobRepository, ClusterManager clusterManager) {
        return new SchedulerEngine("org.github.Alyas100.plugins", jobRepository, clusterManager);
    }

    @Bean
    public JobRepository jobRepository() {
        // Use in-memory storage for demo
        return new org.github.Alyas100.storage.InMemoryJobRepository();
    }

    @Bean
    @Profile("!cluster")  // Only create when 'cluster' profile is NOT active
    public ClusterManager standaloneClusterManager() {
        // Return a simple standalone implementation for single-node
        System.out.println("Starting in STANDALONE mode (no clustering)");
        return new StandaloneClusterManager();
    }

    @Bean
    @Profile("cluster")  // Only create when 'cluster' profile is active
    public ClusterManager hazelcastClusterManager() {
        // Hazelcast clustering for production
        System.out.println("Starting in CLUSTER mode with Hazelcast");
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance();
        return new HazelcastClusterManager(hazelcast);
    }

    /**
     * Simple standalone cluster manager for single-node operation
     */
    private static class StandaloneClusterManager implements ClusterManager {
        private final String nodeId = "standalone-" + System.currentTimeMillis();
        private boolean running = false;

        @Override
        public void start() {
            running = true;
            System.out.println("Standalone cluster manager started");
        }

        @Override
        public void stop() {
            running = false;
            System.out.println("Standalone cluster manager stopped");
        }

        @Override
        public boolean isLeader() {
            return true; // Always leader in standalone mode
        }

        @Override
        public String getNodeId() {
            return nodeId;
        }

        @Override
        public java.util.Set<org.github.Alyas100.cluster.ClusterNode> getClusterNodes() {
            // Single node in standalone mode
            org.github.Alyas100.cluster.ClusterNode node = new org.github.Alyas100.cluster.ClusterNode(
                    nodeId, "localhost", 8080
            );
            node.setStatus(org.github.Alyas100.cluster.ClusterNode.NodeStatus.ACTIVE);
            return java.util.Set.of(node);
        }

        @Override
        public void distributeJob(org.github.Alyas100.JobDefinition job) {
            System.out.println("[STANDALONE] Job scheduled: " + job.jobId());
        }

        @Override
        public void rebalanceJobs() {
            System.out.println("[STANDALONE] No rebalancing needed in standalone mode");
        }

        @Override
        public void addClusterListener(org.github.Alyas100.cluster.ClusterListener listener) {
            // No-op in standalone
        }

        @Override
        public void removeClusterListener(org.github.Alyas100.cluster.ClusterListener listener) {
            // No-op in standalone
        }

        @Override
        public org.github.Alyas100.cluster.ClusterState getClusterState() {
            return new org.github.Alyas100.cluster.ClusterState(
                    nodeId, // leader
                    1,      // totalNodes
                    1,      // activeNodes
                    true    // healthy
            );
        }
    }
}