package org.github.Alyas100.demo;

import org.github.Alyas100.cluster.*;
import org.github.Alyas100.cluster.hazelcast.HazelcastClusterManager;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.github.Alyas100.JobDefinition;
import org.github.Alyas100.RetryPolicy;

import java.time.Duration;
import java.util.Map;

/**
 * Integration test for the cluster module.
 * Tests cluster formation, leader election, and basic functionality.
 */
public class ClusterIntegrationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 STARTING CLUSTER INTEGRATION TEST");
        System.out.println("=====================================");

        // Test 1: Single Node Cluster
        testSingleNodeCluster();

        // Test 2: Test with Scheduler Engine Integration
        testSchedulerEngineIntegration();

        // Test 3: test with multiple nodes
        testMultiNodeCluster();

        System.out.println("✅ ALL CLUSTER TESTS COMPLETED SUCCESSFULLY!");
    }

    /**
     * Test a single node cluster - becomes leader automatically
     */
    private static void testSingleNodeCluster() throws Exception {
        System.out.println("\n🔧 TEST 1: SINGLE NODE CLUSTER");
        System.out.println("-----------------------------");

        HazelcastInstance hazelcast = null;
        ClusterManager clusterManager = null;

        try {
            // Create Hazelcast configuration for testing
            Config config = new Config();
            config.setClusterName("test-cluster-" + System.currentTimeMillis());
            config.getNetworkConfig().setPort(5701).setPortAutoIncrement(false);

            // Create Hazelcast instance and cluster manager
            hazelcast = Hazelcast.newHazelcastInstance(config);
            clusterManager = new HazelcastClusterManager(hazelcast);

            // Add cluster event listener
            clusterManager.addClusterListener(new TestClusterListener());

            // Start the cluster
            System.out.println("🟢 Starting cluster node...");
            clusterManager.start();

            // Wait for cluster formation
            Thread.sleep(2000);

            // Verify cluster state
            ClusterState state = clusterManager.getClusterState();
            System.out.println("📊 Cluster State:");
            System.out.println("   - Node ID: " + clusterManager.getNodeId());
            System.out.println("   - Leader: " + state.leaderId());
            System.out.println("   - Total Nodes: " + state.totalNodes());
            System.out.println("   - Active Nodes: " + state.activeNodes());
            System.out.println("   - Healthy: " + state.healthy());
            System.out.println("   - Am I Leader: " + clusterManager.isLeader());

            // Verify single node behavior
            assert clusterManager.isLeader() : "Single node should be leader!";
            assert state.totalNodes() == 1 : "Should have exactly 1 node!";
            assert state.activeNodes() == 1 : "Should have 1 active node!";
            assert state.healthy() : "Cluster should be healthy!";

            // Test node information
            var nodes = clusterManager.getClusterNodes();
            assert nodes.size() == 1 : "Should have exactly 1 cluster node!";

            ClusterNode localNode = nodes.iterator().next();
            System.out.println("🖥️  Local Node Info:");
            System.out.println("   - ID: " + localNode.getNodeId());
            System.out.println("   - Host: " + localNode.getHost());
            System.out.println("   - Port: " + localNode.getPort());
            System.out.println("   - Status: " + localNode.getStatus());
            System.out.println("   - Uptime: " + localNode.getUptime() + "ms");

            // Test job distribution (as leader)
            if (clusterManager.isLeader()) {
                System.out.println("👑 I am the leader - testing job distribution...");

                // Create test job definitions
                JobDefinition job1 = createTestJob("cluster-job-1", "Test cluster job 1");
                JobDefinition job2 = createTestJob("cluster-job-2", "Test cluster job 2");

                clusterManager.distributeJob(job1);
                clusterManager.distributeJob(job2);
            }

            // Test cluster state methods
            System.out.println("🔍 Testing cluster state utilities...");
            System.out.println("   - Inactive Nodes: " + state.getInactiveNodes());
            System.out.println("   - Has Quorum: " + state.hasQuorum());

            // Simulate some cluster activity
            System.out.println("⏳ Simulating cluster activity (5 seconds)...");
            for (int i = 0; i < 5; i++) {
                Thread.sleep(1000);
                ClusterState currentState = clusterManager.getClusterState();
                System.out.println("   [" + (i + 1) + "s] Cluster healthy: " + currentState.healthy() +
                        ", Nodes: " + currentState.activeNodes() + "/" + currentState.totalNodes());
            }

            // Test rebalancing
            System.out.println("⚖️ Testing job rebalancing...");
            clusterManager.rebalanceJobs();

            System.out.println("✅ SINGLE NODE CLUSTER TEST PASSED!");

        } catch (Exception e) {
            System.out.println("❌ SINGLE NODE CLUSTER TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            // Cleanup
            if (clusterManager != null) {
                System.out.println("🧹 Cleaning up cluster...");
                clusterManager.stop();
            }
            if (hazelcast != null) {
                hazelcast.shutdown();
            }
            Thread.sleep(1000);
        }
    }

    /**
     * Test integration with Scheduler Engine
     */
    private static void testSchedulerEngineIntegration() throws Exception {
        System.out.println("\n🔧 TEST 2: SCHEDULER ENGINE INTEGRATION");
        System.out.println("--------------------------------------");

        // This would test the full integration with SchedulerEngine
        // For now, we'll simulate what it would look like

        HazelcastInstance hazelcast = null;

        try {
            Config config = new Config();
            config.setClusterName("scheduler-test-" + System.currentTimeMillis());
            config.getNetworkConfig().setPort(5801).setPortAutoIncrement(false);

            hazelcast = Hazelcast.newHazelcastInstance(config);
            ClusterManager clusterManager = new HazelcastClusterManager(hazelcast);

            System.out.println("🟢 Starting cluster for scheduler integration test...");
            clusterManager.start();
            Thread.sleep(2000);

            // Simulate what SchedulerEngine would do
            System.out.println("🎯 Testing leader election for job scheduling...");

            if (clusterManager.isLeader()) {
                System.out.println("📅 I am leader - would schedule jobs here");
                // In real scenario, SchedulerEngine would call scheduleJob()
            } else {
                System.out.println("💼 I am worker - would wait for job distribution");
                // In real scenario, SchedulerEngine would wait for distributed jobs
            }

            // Test cluster events
            clusterManager.addClusterListener(new ClusterListener() {
                @Override
                public void onLeaderElected(String leaderId) {
                    System.out.println("   🎉 [EVENT] Leader elected: " + leaderId);
                }

                @Override
                public void onJobDistributed(String jobId, String targetNode) {
                    System.out.println("   📤 [EVENT] Job distributed: " + jobId + " → " + targetNode);
                }
            });

            // Simulate job distribution
            JobDefinition testJob = createTestJob("integration-test-job", "Integration test job");
            clusterManager.distributeJob(testJob);

            Thread.sleep(3000);
            clusterManager.stop();

            System.out.println("✅ SCHEDULER ENGINE INTEGRATION TEST PASSED!");

        } catch (Exception e) {
            System.out.println("❌ SCHEDULER ENGINE INTEGRATION TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (hazelcast != null) {
                hazelcast.shutdown();
            }
        }
    }

    /**
     * Create a test job definition
     */
    private static JobDefinition createTestJob(String jobId, String description) {
        return new JobDefinition(
                jobId,
                description,
                "simple-logger",
                "*/30 * * * * *",  // Every 30 seconds for testing
                Map.of("message", "Hello from cluster test: " + description),
                new RetryPolicy(Duration.ofSeconds(30), Duration.ofMinutes(5), 2.0, true),
                Duration.ofMinutes(2),
                3,
                true
        );
    }

    /**
     * Test cluster event listener
     */
    private static class TestClusterListener implements ClusterListener {
        @Override
        public void onLeaderElected(String leaderId) {
            System.out.println("   🎉 [EVENT] NEW LEADER ELECTED: " + leaderId);
        }

        @Override
        public void onNodeJoined(ClusterNode node) {
            System.out.println("   🟢 [EVENT] NODE JOINED: " + node.getNodeId() +
                    " (" + node.getHost() + ":" + node.getPort() + ")");
        }

        @Override
        public void onNodeLeft(ClusterNode node) {
            System.out.println("   🔴 [EVENT] NODE LEFT: " + node.getNodeId());
        }

        @Override
        public void onJobDistributed(String jobId, String targetNode) {
            System.out.println("   📤 [EVENT] JOB DISTRIBUTED: " + jobId + " → " + targetNode);
        }
    }

    private static void testMultiNodeCluster() throws Exception {
        System.out.println("\n🔧 TEST 3: MULTI-NODE CLUSTER");
        System.out.println("-----------------------------");

        int nodeCount = 3; // simulate 3 nodes
        HazelcastInstance[] nodes = new HazelcastInstance[nodeCount];
        ClusterManager[] managers = new ClusterManager[nodeCount];

        try {
            for (int i = 0; i < nodeCount; i++) {
                Config config = new Config();
                config.setClusterName("multi-node-cluster");
                config.getNetworkConfig().setPort(5701 + i).setPortAutoIncrement(false);

                nodes[i] = Hazelcast.newHazelcastInstance(config);
                managers[i] = new HazelcastClusterManager(nodes[i]);
                managers[i].start();
                managers[i].addClusterListener(new TestClusterListener());

                System.out.println("🟢 Node " + i + " started with ID: " + managers[i].getNodeId());
            }

            Thread.sleep(3000); // wait for all nodes to discover each other

            // Verify cluster state from first node
            ClusterState state = managers[0].getClusterState();
            System.out.println("📊 Cluster State from Node 0:");
            System.out.println("   - Leader: " + state.leaderId());
            System.out.println("   - Total Nodes: " + state.totalNodes());
            System.out.println("   - Active Nodes: " + state.activeNodes());

            assert state.totalNodes() == nodeCount : "All nodes should be visible!";
            assert state.activeNodes() == nodeCount : "All nodes should be active!";

            // Simulate job distribution from leader
            if (managers[0].isLeader()) {
                System.out.println("👑 Node 0 is leader - distributing jobs...");
                for (int j = 1; j <= 3; j++) {
                    JobDefinition job = createTestJob("multi-job-" + j, "Job " + j);
                    managers[0].distributeJob(job);
                }
            }

            Thread.sleep(2000); // allow jobs to propagate

            System.out.println("✅ MULTI-NODE CLUSTER TEST PASSED!");

        } finally {
            // Shutdown all nodes
            for (int i = 0; i < nodeCount; i++) {
                if (managers[i] != null) managers[i].stop();
                if (nodes[i] != null) nodes[i].shutdown();
            }
            Thread.sleep(1000);
        }
    }

}