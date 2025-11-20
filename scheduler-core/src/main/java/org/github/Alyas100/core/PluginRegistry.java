package org.github.Alyas100.core;

import org.github.Alyas100.JobPlugin;
import org.github.Alyas100.PluginConfiguration;
import org.github.Alyas100.annotation.ScheduledJob;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PluginRegistry {
    // this one is used for singleton pattern, where if the pluginInstance already available
    // inside here, it just return from this hashmap without doing any further thing (singleton)
    private final Map<String, JobPlugin> pluginInstances = new ConcurrentHashMap<>();
    // store plugin name with the class plugin
    // jvm load from memory for the class obj and create a class with 'JobPLugin' type and assign
    // it in the hashmap as 'value' of the map
    private final Map<String, Class<? extends JobPlugin>> pluginClasses = new ConcurrentHashMap<>();

    public PluginRegistry(String basePackage) {
        System.out.println("🔍 [DEBUG] PluginRegistry scanning package: " + basePackage);
        scanAndDiscoverPlugins(basePackage);
        System.out.println("📋 [DEBUG] Registered plugins: " + pluginClasses.keySet());
    }

    private void scanAndDiscoverPlugins(String basePackage) {
        try {
            System.out.println("🔍 [DEBUG] Creating Reflections scanner...");

            // FIXED: Use proper Reflections configuration
            Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);

            System.out.println("🔍 [DEBUG] Scanning for @ScheduledJob annotations...");
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(ScheduledJob.class);

            System.out.println("🔍 [DEBUG] Found " + annotatedClasses.size() + " annotated classes");

            for (Class<?> clazz : annotatedClasses) {
                System.out.println("🔍 [DEBUG] Processing class: " + clazz.getName());
                System.out.println("🔍 [DEBUG] Class location: " + clazz.getProtectionDomain().getCodeSource().getLocation());

                if (JobPlugin.class.isAssignableFrom(clazz)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends JobPlugin> pluginClass = (Class<? extends JobPlugin>) clazz;
                    ScheduledJob annotation = clazz.getAnnotation(ScheduledJob.class);
                    System.out.println("✅ [DEBUG] Registering plugin: " + annotation.name() + " from " + clazz.getName());
                    pluginClasses.put(annotation.name(), pluginClass);
                } else {
                    System.out.println("❌ [DEBUG] Class " + clazz.getName() + " does not implement JobPlugin");
                }
            }

            if (annotatedClasses.isEmpty()) {
                System.out.println("❌ [DEBUG] NO ANNOTATED CLASSES FOUND! Possible issues:");
                System.out.println("   - Package " + basePackage + " doesn't exist in classpath");
                System.out.println("   - scheduler-plugins module not included as dependency");
                System.out.println("   - @ScheduledJob annotations missing from classes");

                // Debug: List all classes in the package
                System.out.println("🔍 [DEBUG] Attempting to list all classes in package...");
                try {
                    Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);
                    System.out.println("🔍 [DEBUG] All classes found: " + allClasses.size());
                    for (Class<?> cls : allClasses) {
                        System.out.println("   - " + cls.getName());
                    }
                } catch (Exception e) {
                    System.out.println("❌ [DEBUG] Could not list classes: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("❌ [DEBUG] ERROR during plugin discovery: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets or creates a plugin instance by name.
     */
    public JobPlugin getPlugin(String pluginName, Map<String, Object> config) {
        // this 'getPlugin' method uses 'singleton', which only create first time for the class
        // if it not exsited, return and save into the hashmap (pluginInstances), otherwise just find and return the exsiting one
        return pluginInstances.computeIfAbsent(pluginName, name -> {
            // this below find the plugin class
            // the 'pluginClasses' is a registry discovered via scanning
            // (only can get the class if it have been registered in 'PluginRegistry')
            Class<? extends JobPlugin> pluginClass = pluginClasses.get(name);

            if (pluginClass == null) {
                throw new IllegalArgumentException("Unknown plugin: " + name);
            }

            try {
                // instantiate here to get the plugin object
                // this below uses reflection (in this case it finds on the jar for the classes scanned with some 'annotation')
                // 'plugin' below is an instance of 'SimpleLoggerPlugin'
                // this below make it flexible cause reflection can get all the plugin that registered
                // at runtime without needing for hardcoding every plugins here
                JobPlugin plugin = pluginClass.getDeclaredConstructor().newInstance();

                // intialize the plugin here
                // this gives chance to plugin to read the config (that instantiated by constructor) before running
                plugin.initialize(new PluginConfiguration(config));
                return plugin;
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate plugin: " + name, e);
            }
        });
    }

    public Set<String> getAvailablePlugins() {
        return Collections.unmodifiableSet(pluginClasses.keySet());
    }

    public void shutdown() {
        pluginInstances.values().forEach(JobPlugin::shutdown);
        pluginInstances.clear();
    }
}