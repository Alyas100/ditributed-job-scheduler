package org.github.Alyas100.core;

import org.github.Alyas100.JobDefinition;

import java.util.Optional;

/**
 * Listener interface for scheduler events.
 */
 public interface SchedulerListener {
    default void onSchedulerStart() {}
    default void onSchedulerStop() {}
    default void onJobScheduled(JobDefinition job) {}
    default void onJobUnscheduled(JobDefinition job) {}
    default void onJobExecuting(Optional<JobDefinition> job) {}
}
