package dev.byblos.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link SummaryStats}.
 */
public class SummaryStatsTest {

    private static final long start = 0L;
    private static final long step = 60000L;
    private static final long end = start + 2 * step;

    @Test
    void constant() {
        var stats = SummaryStats.fromData(ts(1.0, 1.0), start, end);
        assertEquals(stats.count(), 2);
        assertEquals(stats.min(), 1.0);
        assertEquals(stats.max(), 1.0);
        assertEquals(stats.last(), 1.0);
        assertEquals(stats.total(), 2.0);
    }

    @Test
    void varied() {
        var stats = SummaryStats.fromData(ts(1.0, 2.0), start, end);
        assertEquals(stats.count(), 2);
        assertEquals(stats.min(), 1.0);
        assertEquals(stats.max(), 2.0);
        assertEquals(stats.last(), 2.0);
        assertEquals(stats.total(), 3.0);
    }

    @Test
    void negative() {
        var stats = SummaryStats.fromData(ts(-1.0, -2.0), start, end);
        assertEquals(stats.count(), 2);
        assertEquals(stats.min(), -2.0);
        assertEquals(stats.max(), -1.0);
        assertEquals(stats.last(), -2.0);
        assertEquals(stats.total(), -3.0);
    }

    @Test
    void NaNFirst() {
        var stats = SummaryStats.fromData(ts(Double.NaN, -2.0), start, end);
        assertEquals(stats.count(), 1);
        assertEquals(stats.min(), -2.0);
        assertEquals(stats.max(), -2.0);
        assertEquals(stats.last(), -2.0);
        assertEquals(stats.total(), -2.0);
    }

    @Test
    void NaNLast() {
        var stats = SummaryStats.fromData(ts(-1.0, Double.NaN), start, end);
        assertEquals(stats.count(), 1);
        assertEquals(stats.min(), -1.0);
        assertEquals(stats.max(), -1.0);
        assertEquals(stats.last(), -1.0);
        assertEquals(stats.total(), -1.0);
    }

    @Test
    void infinity() {
        var stats = SummaryStats.fromData(ts(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), start, end);
        assertEquals(stats.count(), 2);
        assertEquals(stats.min(), Double.POSITIVE_INFINITY);
        assertEquals(stats.max(), Double.POSITIVE_INFINITY);
        assertEquals(stats.last(), Double.POSITIVE_INFINITY);
        assertEquals(stats.total(), Double.POSITIVE_INFINITY);
    }

    @Test
    void constantInfinity() {
        var stats = SummaryStats.fromData(ts(1.0, Double.POSITIVE_INFINITY), start, end);
        assertEquals(stats.count(), 2);
        assertEquals(stats.min(), 1.0);
        assertEquals(stats.max(), Double.POSITIVE_INFINITY);
        assertEquals(stats.last(), Double.POSITIVE_INFINITY);
        assertEquals(stats.total(), Double.POSITIVE_INFINITY);
    }

    @Test
    void infinityConstant() {
        var stats = SummaryStats.fromData(ts(Double.POSITIVE_INFINITY, 1.0), start, end);
        assertEquals(stats.count(), 2);
        assertEquals(stats.min(), 1.0);
        assertEquals(stats.max(), Double.POSITIVE_INFINITY);
        assertEquals(stats.last(), 1.0);
        assertEquals(stats.total(), Double.POSITIVE_INFINITY);
    }

    private static TimeSeries ts(double... values) {
        var data = new ArrayTimeSeq(start, step, values);
        var tags = Map.of("name", "test");
        return new TimeSeries(data, TimeSeries.defaultLabel(tags), tags);
    }
}
