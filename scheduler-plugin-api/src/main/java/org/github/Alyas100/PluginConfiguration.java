package org.github.Alyas100;

import java.util.Map;
import java.util.Optional;

/**
 * Type-safe configuration wrapper for plugin initialization.
 * Prevents casting errors and provides validation.
 */
public class PluginConfiguration {
    private final Map<String, Object> configMap;

    public PluginConfiguration(Map<String, Object> configMap) {
        this.configMap = Map.copyOf(configMap); // Immutable
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.ofNullable(configMap.get(key))
                .filter(type::isInstance)
                .map(type::cast);
    }

    public String getString(String key) {
        return get(key, String.class).orElse(null);
    }

    public String getString(String key, String defaultValue) {
        return get(key, String.class).orElse(defaultValue);
    }

    public Integer getInt(String key) {
        return get(key, Integer.class).orElse(null);
    }

    public int getInt(String key, int defaultValue) {
        return get(key, Integer.class).orElse(defaultValue);
    }

    public Boolean getBoolean(String key) {
        return get(key, Boolean.class).orElse(false);
    }

    public Long getLong(String key) {
        return get(key, Long.class).orElse(null);
    }

    public Double getDouble(String key) {
        return get(key, Double.class).orElse(null);
    }

    // Validation
    public void require(String key) {
        if (!configMap.containsKey(key)) {
            throw new IllegalArgumentException("Required configuration missing: " + key);
        }
    }

    public Map<String, Object> toMap() {
        return Map.copyOf(configMap);
    }
}