package org.github.Alyas100;

/**
 * Error type classification for better failure handling and retry logic.
 */
public enum ErrorType {
    PLUGIN_ERROR,           // Plugin code threw exception
    CONFIGURATION_ERROR,    // Invalid plugin configuration
    RESOURCE_UNAVAILABLE,   // Required resources not available
    TIMEOUT,                // Job execution timed out
    NETWORK_ERROR,          // Network communication failed
    PERMISSION_DENIED,      // Insufficient permissions
    DATA_VALIDATION_ERROR,  // Input data validation failed
    UNKNOWN_ERROR           // Unclassified error
}
