package dev.byblos.core.model;

import java.util.Map;
import java.util.function.Function;

public final class SummaryStats {
    private final int count;
    private final double min;
    private final double max;
    private final double last;
    private final double total;

    public SummaryStats(int count, double min, double max, double last, double total) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.last = last;
        this.total = total;
    }

    public static final SummaryStats EMPTY = new SummaryStats(0, Double.NaN, Double.NaN, Double.NaN, Double.NaN);

    public static SummaryStats fromData(TimeSeries ts, long start, long end) {
        return fromData(ts.data(), start, end);
    }

    public static SummaryStats fromData(TimeSeq ts, long start, long end) {
        var total = 0.0;
        var count = 0;
        var max = Double.NEGATIVE_INFINITY;
        var min = Double.POSITIVE_INFINITY;
        var last = Double.NaN;

        for (var v : ts.range(start, end)) {
            if (!Double.isNaN(v)) {
                total += v;
                count += 1;
                max = (v > max) ? v : max;
                min = (v < min) ? v : min;
                last = v;
            }
        }

        return (count == 0) ? EMPTY : new SummaryStats(count, min, max, last, total);
    }

    public int count() {
        return count;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double last() {
        return last;
    }

    public double total() {
        return total;
    }

    public double avg() {
        return (count > 0) ? total / count : Double.NaN;
    }

    /**
     * Tags corresponding to the stats available from `:stat` operator.
     */
    public Map<String, String> tags(Function<Double, String> formatter) {
        return Map.of(
                TagKey.avg, formatter.apply(avg()),
                TagKey.max, formatter.apply(max()),
                TagKey.min, formatter.apply(min()),
                TagKey.last, formatter.apply(last()),
                TagKey.total, formatter.apply(total())
        );
    }

    /**
     * Return the value of a statistic based on the name.
     */
    public double get(String stat) {
        switch (stat) {
            case "avg":
                return avg();
            case "max":
                return max();
            case "min":
                return min();
            case "last":
                return last();
            case "total":
                return total();
            case "count":
                return count();
            default:
                throw new IllegalArgumentException("unknown statistic: " + stat);
        }
    }
}


