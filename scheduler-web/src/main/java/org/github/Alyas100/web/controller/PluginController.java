package org.github.Alyas100.web.controller;

import org.github.Alyas100.core.SchedulerEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/plugins")
@CrossOrigin(origins = "*")
public class PluginController {

    private final SchedulerEngine schedulerEngine;

    public PluginController(SchedulerEngine schedulerEngine) {
        this.schedulerEngine = schedulerEngine;
    }

    @GetMapping
    public ResponseEntity<Set<String>> getAvailablePlugins() {
        return ResponseEntity.ok(schedulerEngine.getAvailablePlugins());
    }

    @GetMapping("/{pluginName}/info")
    public ResponseEntity<Map<String, String>> getPluginInfo(@PathVariable String pluginName) {
        Set<String> plugins = schedulerEngine.getAvailablePlugins();
        if (plugins.contains(pluginName)) {
            return ResponseEntity.ok(Map.of(
                    "name", pluginName,
                    "status", "AVAILABLE",
                    "loaded", "true"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "name", pluginName,
                    "status", "NOT_FOUND",
                    "loaded", "false"
            ));
        }
    }
}