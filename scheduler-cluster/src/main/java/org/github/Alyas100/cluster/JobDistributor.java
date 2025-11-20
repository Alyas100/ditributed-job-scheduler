package org.github.Alyas100.cluster;

import org.github.Alyas100.JobDefinition;
import org.github.Alyas100.JobExecutionResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Distributes jobs across cluster nodes using various strategies.
 */
public interface JobDistributor {

    enum DistributionStrategy {
        ROUND_ROBIN,      // Distribute evenly
        LOAD_BASED,       // Based on node load
        AFFINITY,         // Stick to same node
        BROADCAST         // Run on all nodes
    }

    CompletableFuture<JobExecutionResult> distributeJob(
            JobDefinition job,
            DistributionStrategy strategy
    );

    void reassignJobsFromNode(String failedNodeId);
    Map<String, Integer> getNodeLoad();
}