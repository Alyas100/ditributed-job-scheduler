package org.github.Alyas100;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Sealed hierarchy representing all possible job execution outcomes.
 *
 * <p>Modern Java pattern matching enables elegant handling of different result types.</p>
 *
 * <p><b>Usage Examples:</b>
 * <pre>{@code
 * JobExecutionResult result = plugin.execute(context);
 *
 * switch (result) {
 *     case Success s ->
 *         System.out.println("Success: " + s.message() + " Output: " + s.outputData());
 *     case Failure f ->
 *         System.out.println("Failed: " + f.errorMessage() + " Retry: " + f.shouldRetry());
 *     case PartialSuccess p ->
 *         System.out.println("Partial: " + p.message() + " Warning: " + p.warning());
 * }
 * }</pre>
 * </p>
 */

/*here all the records is implemented inside the interface because for easier pattern matching when
using the 'switch' statement over the sealed results
 */
public sealed interface JobExecutionResult {

    /**
     * Successful job execution with output data.
     *
     * @param message Success description
     * @param outputData Job output (serializable for distributed storage)
     * @param executionDuration How long the job took
     * @param outputSizeBytes Size of output data for monitoring
     */
    // all 'record' automatically makes all its fields 'final' to ensure immutability
    record Success(
            String message,
            Map<String, Object> outputData,
            Duration executionDuration,
            long outputSizeBytes
    ) implements JobExecutionResult {}

    /**
     * Failed job execution with error details.
     *
     * @param errorMessage Error description
     * @param cause Root cause exception (may be null in distributed scenarios)
     * @param executionDuration How long the job ran before failing
     * @param shouldRetry Whether the scheduler should attempt retry
     * @param errorType Classification of error type
     * @param retryCount How many times this job has been retried
     */
    record Failure(
            String errorMessage,
            Throwable cause,
            Duration executionDuration,
            boolean shouldRetry,
            ErrorType errorType,
            int retryCount
    ) implements JobExecutionResult {}

    /**
     * Partially successful execution - some work succeeded, some failed.
     * Common in batch processing and distributed workflows.
     *
     * @param message Status description
     * @param partialOutput Successfully processed data
     * @param warning Warning message about partial failure
     * @param executionDuration Total execution time
     * @param successCount Number of successful items
     * @param failureCount Number of failed items
     */
    record PartialSuccess(
            String message,
            Map<String, Object> partialOutput,
            String warning,
            Duration executionDuration,
            int successCount,
            int failureCount
    ) implements JobExecutionResult {}

    /**
     * Job was cancelled before completion.
     * This can happen during cluster rebalancing or manual intervention.
     *
     * @param reason Cancellation reason
     * @param cancelledBy Who initiated the cancellation (system, user, etc.)
     * @param executionDuration How long it ran before cancellation
     * @param progressPercentage How much work was completed
     */
    record Cancelled(
            String reason,
            String cancelledBy,
            Duration executionDuration,
            int progressPercentage
    ) implements JobExecutionResult {}
}

