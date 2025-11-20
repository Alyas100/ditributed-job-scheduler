package org.github.Alyas100.core;

import java.time.LocalDateTime;
import java.util.BitSet;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Simple cron expression parser for scheduling jobs.
 * Supports standard cron format: "second minute hour day month dayOfWeek"
 */
public class CronExpression {
    private final BitSet seconds = new BitSet(60);
    private final BitSet minutes = new BitSet(60);
    private final BitSet hours = new BitSet(24);
    private final BitSet daysOfMonth = new BitSet(32);
    private final BitSet months = new BitSet(13);
    private final BitSet daysOfWeek = new BitSet(8);

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");

    public CronExpression(String expression) {
        parse(expression);
    }

    private void parse(String expression) {
        String[] fields = expression.trim().split("\\s+");

        if (fields.length != 6) {
            throw new IllegalArgumentException(
                    "Cron expression must have 6 fields: second minute hour day month dayOfWeek");
        }

        parseField(fields[0], seconds, 0, 59);
        parseField(fields[1], minutes, 0, 59);
        parseField(fields[2], hours, 0, 23);
        parseField(fields[3], daysOfMonth, 1, 31);
        parseField(fields[4], months, 1, 12);
        parseField(fields[5], daysOfWeek, 0, 6); // 0=Sunday, 6=Saturday
    }

    private void parseField(String field, BitSet bits, int min, int max) {
        if ("*".equals(field)) {
            for (int i = min; i <= max; i++) {
                bits.set(i);
            }
            return;
        }

        if (field.contains(",")) {
            for (String part : field.split(",")) {
                parseField(part, bits, min, max);
            }
            return;
        }

        if (field.contains("-")) {
            String[] range = field.split("-");
            if (range.length != 2) {
                throw new IllegalArgumentException("Invalid range: " + field);
            }
            int start = parseInt(range[0], min, max);
            int end = parseInt(range[1], min, max);
            for (int i = start; i <= end; i++) {
                bits.set(i);
            }
            return;
        }

        if (field.contains("/")) {
            String[] parts = field.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid step: " + field);
            }

            BitSet rangeBits = new BitSet(max + 1);
            parseField(parts[0], rangeBits, min, max);
            int step = parseInt(parts[1], 1, max);

            for (int i = rangeBits.nextSetBit(min); i >= 0; i = rangeBits.nextSetBit(i + 1)) {
                if (i % step == 0) {
                    bits.set(i);
                }
            }
            return;
        }

        int value = parseInt(field, min, max);
        bits.set(value);
    }

    private int parseInt(String value, int min, int max) {
        if (!NUMBER_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid number: " + value);
        }
        int num = Integer.parseInt(value);
        if (num < min || num > max) {
            throw new IllegalArgumentException(
                    "Value " + num + " out of range [" + min + "-" + max + "]");
        }
        return num;
    }

    public LocalDateTime next(LocalDateTime after) {
        LocalDateTime next = after.plusSeconds(1);

        while (true) {
            if (!months.get(next.getMonthValue())) {
                next = next.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                continue;
            }

            if (!daysOfMonth.get(next.getDayOfMonth())) {
                next = next.plusDays(1).withHour(0).withMinute(0).withSecond(0);
                continue;
            }

            int dayOfWeek = next.getDayOfWeek().getValue() % 7;
            if (!daysOfWeek.get(dayOfWeek)) {
                next = next.plusDays(1).withHour(0).withMinute(0).withSecond(0);
                continue;
            }

            if (!hours.get(next.getHour())) {
                next = next.plusHours(1).withMinute(0).withSecond(0);
                continue;
            }

            if (!minutes.get(next.getMinute())) {
                next = next.plusMinutes(1).withSecond(0);
                continue;
            }

            if (!seconds.get(next.getSecond())) {
                next = next.plusSeconds(1);
                continue;
            }

            break;
        }

        return next;
    }

//    public static CronExpression parse(String expression) {
//        return new CronExpression(expression);
//    }
}