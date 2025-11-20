package org.github.Alyas100;

import java.time.Duration; /**
 * This define retry timing logic (how to handle failure)
 * @param initialDelay
 * @param maxDelay
 * @param backoffMultiplier
 * @param exponentialBackoff
 */
public  record RetryPolicy(
        Duration initialDelay,
        Duration maxDelay,
        double backoffMultiplier,
        boolean exponentialBackoff
) {
    public RetryPolicy {
        if (initialDelay == null) initialDelay = Duration.ofSeconds(30);
        if (maxDelay == null) maxDelay = Duration.ofMinutes(10);
    }
}
