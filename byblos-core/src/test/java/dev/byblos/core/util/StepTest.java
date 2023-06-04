package dev.byblos.core.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Step}.
 */
public class StepTest {
    @Test
    void roundAllowArbitraryNumberOfDays() {
        for (var i = 1; i < 500; i++) {
            assertEquals(days(i), Step.round(60000, days(i)));
        }
    }

    @Test
    void roundUpIfLessThanADay() {
        assertEquals(days(1), Step.round(60000, days(1) / 2 + 1));
    }

    @Test
    void roundIpIfNotOnDayBoundary() {
        assertEquals(days(3), Step.round(60000, days(2) + 1));
    }

    @Test
    void computeAllowArbitraryNumberOfDays() {
        for (var i = 1; i < 500; i++) {
            assertEquals(days(i), Step.compute(60000, 1, 0L, days(i)));
        }
    }

    @Test
    void computeRoundLessThanADay() {
        var e = Instant.parse("2017-06-27T00:00:00Z");
        var s = e.minus(12 * 30, ChronoUnit.DAYS);
        assertEquals(days(1), Step.compute(60000, 430, s.toEpochMilli(), e.toEpochMilli()));
    }

    private static long days(long n) {
        return n * 24 * 60 * 60 * 1000;
    }
}
