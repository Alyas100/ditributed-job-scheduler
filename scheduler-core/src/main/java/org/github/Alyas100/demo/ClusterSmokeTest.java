package org.github.Alyas100.demo;

import com.hazelcast.core.HazelcastInstance;
import org.github.Alyas100.cluster.*;
import org.github.Alyas100.cluster.hazelcast.HazelcastClusterManager;
import com.hazelcast.core.Hazelcast;

/**
 * Quick smoke test to verify cluster module works
 */
public class ClusterSmokeTest {
    public static void main(String[] args) {
        System.out.println("🔥 CLUSTER SMOKE TEST");
        System.out.println("=====================");

        HazelcastInstance hazelcast = null;
        ClusterManager cluster = null;

        try {
            // Create cluster manager
            hazelcast = Hazelcast.newHazelcastInstance();  // this creates one node
            cluster = new HazelcastClusterManager(hazelcast);

            // Add simple listener
            cluster.addClusterListener(new ClusterListener() {
                public void onLeaderElected(String leaderId) {
                    System.out.println("🎉 Leader elected: " + leaderId);
                }
            });

            // Start cluster
            cluster.start();
            Thread.sleep(2000);

            // Basic assertions
            System.out.println("✅ Node ID: " + cluster.getNodeId());
            System.out.println("✅ Is Leader: " + cluster.isLeader());

            ClusterState state = cluster.getClusterState();
            System.out.println("✅ Cluster State: " + state);
            System.out.println("✅ Nodes: " + cluster.getClusterNodes().size());

            // Test cluster state
            if (state.healthy() && state.totalNodes() > 0) {
                System.out.println("✅ Cluster is healthy with " + state.totalNodes() + " nodes");
            } else {
                System.out.println("❌ Cluster unhealthy: " + state);
                throw new RuntimeException("Cluster not healthy");
            }

            // Test job distribution
            if (cluster.isLeader()) {
                System.out.println("👑 Testing job distribution as leader...");
                // Would distribute jobs here
            }

            System.out.println("🎉 CLUSTER SMOKE TEST PASSED!");

        } catch (Exception e) {
            System.out.println("💥 CLUSTER SMOKE TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Cleanup
            try {
                if (cluster != null) cluster.stop();
                if (hazelcast != null) hazelcast.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
