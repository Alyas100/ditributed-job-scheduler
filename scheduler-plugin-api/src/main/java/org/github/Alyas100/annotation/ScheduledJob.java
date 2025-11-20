package org.github.Alyas100.annotation;

import java.lang.annotation.*;

/**
 * Marks a class as a schedulable job plugin for automatic discovery.
 *
 * <p>This annotation enables the scheduler to automatically detect and register
 * plugins without manual configuration.</p>
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * @ScheduledJob(
 *     name = "http-request",
 *     description = "Makes HTTP requests to configured endpoints",
 *     version = "1.0"
 * )
 * public class HttpJobPlugin implements JobPlugin {
 *     // Plugin implementation
 * }
 * }</pre>
 * </p>
 */
@Target(ElementType.TYPE)           // Can only be applied to classes
@Retention(RetentionPolicy.RUNTIME) // Available at runtime via reflection
@Documented                          // Include in generated Javadoc
public @interface ScheduledJob {

    /**
     * Unique name for this job type.
     * This will be used to reference the plugin in job definitions.
     */
    String name();

    /**
     * Human-readable description of what this plugin does.
     */
    String description() default "";

    /**
     * Plugin version for compatibility and updates.
     */
    String version() default "1.0";

    /**
     * Default cron expression for scheduling.
     * Can be overridden in individual job definitions.
     */
    String defaultCron() default "";

    /**
     * Whether this plugin supports parallel execution.
     * If false, only one instance can run at a time.
     */
    boolean supportsParallel() default true;

    /**
     * Estimated resource requirements for scheduling decisions.
     */
    ResourceRequirement resources() default @ResourceRequirement;

    /**
     * Categories for organizing plugins in UI.
     */
    String[] categories() default {};
}

/**
 * Resource requirements annotation for scheduling optimization.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@interface ResourceRequirement {
    int cpuUnits() default 1;           // Relative CPU requirement (1-10 scale)
    int memoryMB() default 128;         // Memory requirement in MB
    boolean requiresNetwork() default false;
    boolean requiresDiskIO() default false;
}