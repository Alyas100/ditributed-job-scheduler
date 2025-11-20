package org.github.Alyas100.cluster.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cluster.Member;
import com.hazelcast.topic.ITopic;
import org.github.Alyas100.cluster.ClusterState;
import org.github.Alyas100.cluster.*;
import org.github.Alyas100.JobDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HazelcastClusterManager implements ClusterManager {
    private final HazelcastInstance hazelcast;
    private final String nodeId;
    private final Set<ClusterListener> listeners = ConcurrentHashMap.newKeySet();
    private final ITopic<ClusterEvent> eventTopic;

    public HazelcastClusterManager(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
        this.nodeId = hazelcast.getCluster().getLocalMember().getUuid().toString();
        this.eventTopic = hazelcast.getTopic("cluster-events");
        setupEventListening();
    }

    @Override
    public void start() {
        System.out.println("🚀 Starting Hazelcast cluster node: " + nodeId);
        eventTopic.publish(new ClusterEvent.NodeJoined(nodeId));
    }

    @Override
    public void stop() {
        eventTopic.publish(new ClusterEvent.NodeLeft(nodeId));
        System.out.println("🛑 Stopping Hazelcast cluster node: " + nodeId);
    }

    @Override
    public boolean isLeader() {
        // Simple leader election: node with smallest UUID becomes leader
        return getClusterNodes().stream()
                .min(Comparator.comparing(ClusterNode::getNodeId))  // ✅ FIXED: getNodeId()
                .map(leader -> leader.getNodeId().equals(nodeId))    // ✅ FIXED: getNodeId()
                .orElse(false);
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public Set<ClusterNode> getClusterNodes() {
        Set<ClusterNode> nodes = new HashSet<>();
        for (Member member : hazelcast.getCluster().getMembers()) {
            ClusterNode node = new ClusterNode(
                    member.getUuid().toString(),
                    member.getAddress().getHost(),
                    member.getAddress().getPort()
            );
            // Set additional fields
            node.setStatus(ClusterNode.NodeStatus.ACTIVE);
            node.updateHeartbeat(); // Set last heartbeat
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public void distributeJob(JobDefinition job) {
        if (isLeader()) {
            System.out.println("📤 [CLUSTER] Leader distributing job: " + job.jobId());
            eventTopic.publish(new ClusterEvent.JobDistributed(job.jobId(), nodeId));
        }
    }

    @Override
    public void rebalanceJobs() {
        System.out.println("⚖️ [CLUSTER] Rebalancing jobs across cluster...");
        // Implement job rebalancing logic
    }

    @Override
    public void addClusterListener(ClusterListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeClusterListener(ClusterListener listener) {
        listeners.remove(listener);
    }

    @Override
    public ClusterState getClusterState() {
        Set<ClusterNode> nodes = getClusterNodes();
        String leaderId = nodes.stream()
                .min(Comparator.comparing(ClusterNode::getNodeId))  // ✅ FIXED: getNodeId()
                .map(ClusterNode::getNodeId)                        // ✅ FIXED: getNodeId()
                .orElse("unknown");

        return new ClusterState(
                leaderId,
                nodes.size(),
                (int) nodes.stream().filter(n -> n.getStatus() == ClusterNode.NodeStatus.ACTIVE).count(), // ✅ FIXED: getStatus()
                true
        );
    }

    private void setupEventListening() {
        eventTopic.addMessageListener(message -> {
            ClusterEvent event = message.getMessageObject();
            listeners.forEach(listener -> handleEvent(listener, event));
        });
    }

    private void handleEvent(ClusterListener listener, ClusterEvent event) {
        if (event instanceof ClusterEvent.NodeJoined joined) {
            listener.onNodeJoined(findNode(joined.nodeId()));
        } else if (event instanceof ClusterEvent.NodeLeft left) {
            listener.onNodeLeft(findNode(left.nodeId()));
        } else if (event instanceof ClusterEvent.JobDistributed distributed) {
            listener.onJobDistributed(distributed.jobId(), distributed.targetNode());
        }
    }

    private ClusterNode findNode(String nodeId) {
        return getClusterNodes().stream()
                .filter(node -> node.getNodeId().equals(nodeId))  // ✅ FIXED: getNodeId()
                .findFirst()
                .orElse(null);
    }
}