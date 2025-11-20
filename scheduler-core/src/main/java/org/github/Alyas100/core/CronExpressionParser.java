package org.github.Alyas100.core;

/**
 * Utility class for parsing cron expressions.
 * This is just a thin wrapper around CronExpression for better naming.
 */
public class CronExpressionParser {

    /**
     * Parse a cron expression string into a CronExpression object.
     *
     * @param expression cron expression in format: "second minute hour day month dayOfWeek"
     * @return parsed CronExpression
     * @throws IllegalArgumentException if expression is invalid
     */
    public static CronExpression parse(String expression) {
        return new CronExpression(expression);
    }

    /**
     * Validate a cron expression without creating the object.
     */
    public static boolean isValid(String expression) {
        try {
            new CronExpression(expression);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}