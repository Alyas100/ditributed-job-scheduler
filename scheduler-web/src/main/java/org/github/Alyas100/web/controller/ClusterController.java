package org.github.Alyas100.web.controller;

import org.github.Alyas100.core.SchedulerEngine;
import org.github.Alyas100.cluster.ClusterState;
import org.github.Alyas100.web.dto.ClusterInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cluster")
@CrossOrigin(origins = "*")
public class ClusterController {

    private final SchedulerEngine schedulerEngine;

    public ClusterController(SchedulerEngine schedulerEngine) {
        this.schedulerEngine = schedulerEngine;
    }

    @GetMapping("/status")
    public ResponseEntity<ClusterInfo> getClusterStatus() {
        ClusterState clusterState = schedulerEngine.getClusterState();
        if (clusterState == null) {
            return ResponseEntity.ok(ClusterInfo.singleNode());
        }
        return ResponseEntity.ok(ClusterInfo.fromClusterState(clusterState));
    }

    @GetMapping("/nodes")
    public ResponseEntity<?> getClusterNodes() {
        ClusterState clusterState = schedulerEngine.getClusterState();
        if (clusterState == null) {
            return ResponseEntity.ok(Map.of("mode", "STANDALONE", "nodes", 1));
        }
        return ResponseEntity.ok(Map.of(
                "mode", "CLUSTER",
                "totalNodes", clusterState.totalNodes(),
                "activeNodes", clusterState.activeNodes(),
                "leader", clusterState.leaderId(),
                "healthy", clusterState.healthy()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        boolean healthy = schedulerEngine.isRunning();
        return ResponseEntity.ok(Map.of(
                "status", healthy ? "UP" : "DOWN",
                "scheduler", healthy ? "RUNNING" : "STOPPED",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}