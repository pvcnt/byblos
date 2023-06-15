package dev.byblos.chart.graphics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dev.byblos.chart.model.Scale;
import dev.byblos.core.util.UnitPrefix;

import javax.annotation.Nullable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Utility for computing the major tick marks to use for a range of values.
 */
public final class Ticks {

    public static class Pair<U, V> {
        public final U first;
        public final V second;

        private Pair(U first, V second) {
            this.first = requireNonNull(first);
            this.second = requireNonNull(second);
        }
    }

    private static long seconds(int v) {
        return Duration.ofSeconds(v).toMillis();
    }

    private static long minutes(int v) {
        return Duration.ofMinutes(v).toMillis();
    }

    private static long hours(int v) {
        return Duration.ofHours(v).toMillis();
    }

    private static long days(int v) {
        return Duration.ofDays(v).toMillis();
    }

    // Major and minor tick sizes for time axis.
    private static final List<Pair<Long, Long>> timeTickSizes = List.of(
            new Pair<>(minutes(1), seconds(10)),
            new Pair<>(minutes(2), seconds(30)),
            new Pair<>(minutes(3), minutes(1)),
            new Pair<>(minutes(5), minutes(1)),
            new Pair<>(minutes(10), minutes(2)),
            new Pair<>(minutes(15), minutes(5)),
            new Pair<>(minutes(20), minutes(5)),
            new Pair<>(minutes(30), minutes(10)),
            new Pair<>(hours(1), minutes(10)),
            new Pair<>(hours(2), minutes(30)),
            new Pair<>(hours(3), hours(1)),
            new Pair<>(hours(4), hours(1)),
            new Pair<>(hours(6), hours(2)),
            new Pair<>(hours(8), hours(2)),
            new Pair<>(hours(12), hours(3)),
            new Pair<>(days(1), hours(4)),
            new Pair<>(days(2), hours(8)),
            new Pair<>(days(7), days(1)),
            new Pair<>(days(2 * 7), days(2)),
            new Pair<>(days(4 * 7), days(1 * 7))
    );

    static final List<Pair<ChronoField, DateTimeFormatter>> timeBoundaries = List.of(
            new Pair<>(ChronoField.SECOND_OF_MINUTE, DateTimeFormatter.ofPattern(":ss", Locale.ROOT)),
            new Pair<>(ChronoField.MINUTE_OF_HOUR, DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)),
            new Pair<>(ChronoField.HOUR_OF_DAY, DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT))
    );

    // Major and minor tick sizes for value axis
    private static final List<Pair<Integer, Integer>> baseValueTickSizes = List.of(
            new Pair<>(10, 2),
            new Pair<>(20, 5),
            new Pair<>(30, 10),
            new Pair<>(40, 10),
            new Pair<>(50, 10)
    );

    static final DateTimeFormatter defaultTimeFmt = DateTimeFormatter.ofPattern("MMMdd", Locale.ROOT);
    private static final DateTimeFormatter monthTimeFmt = DateTimeFormatter.ofPattern("MMM", Locale.ROOT);
    private static final DateTimeFormatter yearTimeFmt = DateTimeFormatter.ofPattern("yyyy", Locale.ROOT);

    /**
     * Generate value tick marks with approximately `n` major ticks for the range `[s, e]`. Tick
     * marks will be on significant time boundaries for the specified time zone.
     */
    public static List<TimeTick> time(long s, long e, ZoneId zone, int n) {
        // To keep even placement of major grid lines the shift amount for the timezone is computed
        // based on the start. If there is a change such as DST during the interval, then labels
        // after the change may be on less significant boundaries.
        var shift = zone.getRules().getOffset(Instant.ofEpochMilli(s)).getTotalSeconds() * 1000L;

        var dur = e - s;
        var head = timeTickSizes.stream().filter(t -> dur / t.first <= n).findFirst();
        if (head.isPresent()) {
            var major = head.get().first;
            var minor = head.get().second;
            var ticks = new ArrayList<TimeTick>();

            var zs = s + shift;
            var ze = e + shift;
            var pos = zs / major * major;
            while (pos <= ze) {
                if (pos >= zs) {
                    ticks.add(new TimeTick(pos - shift, zone, pos % major == 0L));
                }
                pos += minor;
            }
            return ticks;
        }
        var start = LocalDateTime.ofInstant(Instant.ofEpochMilli(s), zone).toLocalDate();
        var end = LocalDateTime.ofInstant(Instant.ofEpochMilli(e), zone).toLocalDate();
        var days = dur / (24 * 60 * 60 * 1000L);

        long amount;
        ChronoUnit unit;
        DateTimeFormatter fmt;
        if (days <= n) {
            amount = 1L;
            unit = ChronoUnit.DAYS;
            fmt = defaultTimeFmt;
        } else if (days / 30 <= n) {
            amount = 1L;
            unit = ChronoUnit.MONTHS;
            fmt = monthTimeFmt;
        } else if (days / 90 <= n) {
            amount = 3L;
            unit = ChronoUnit.MONTHS;
            fmt = monthTimeFmt;
        } else if (days / 365 <= n) {
            amount = 1L;
            unit = ChronoUnit.YEARS;
            fmt = yearTimeFmt;
        } else {
            amount = days / (n * 365);
            unit = ChronoUnit.YEARS;
            fmt = yearTimeFmt;
        }

        var ticks = new ArrayList<TimeTick>();
        var t = start;
        while (t.isBefore(end) || t.isEqual(end)) {
            var timestamp = t.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() - shift;
            ticks.add(new TimeTick(timestamp, zone, true, fmt));
            t = t.plus(amount, unit);
        }
        return ticks;
    }

    /**
     * Generate value tick marks with approximately `n` major ticks for the range `[v1, v2]`.
     * Uses decimal unit prefixes.
     */
    public static List<ValueTick> value(double v1, double v2, int n) {
        return value(v1, v2, n, Scale.LINEAR);
    }

    public static List<ValueTick> value(double v1, double v2, int n, Scale scale) {
        var r = validateAndGetRange(v1, v2);
        return valueTickSizes.stream()
                .filter(t -> r / t.major <= n)
                .findFirst()
                .map(t -> decimalTicks(v1, v2, n, t, scale))
                .orElseGet(() -> sciTicks(v1, v2, n));
    }

    /**
     * Same as `value(Double,Double,Int)` except that it uses binary unit prefixes.
     */
    public static List<ValueTick> binary(double v1, double v2, int n) {
        var r = validateAndGetRange(v1, v2);
        return binaryValueTickSizes.stream()
                .filter(t -> r / t.major <= n)
                .findFirst()
                .map(t -> binaryTicks(v1, v2, t))
                .orElseGet(() -> sciTicks(v1, v2, n));
    }

    public static List<ValueTick> duration(double v1, double v2, int n) {
        var r = validateAndGetRange(v1, v2);
        return durationValueTickSizes.stream()
                .filter(t -> r / t.major <= n)
                .findFirst()
                .map(t -> durationTicks(v1, v2, t, null))
                .orElseGet(() -> sciTicks(v1, v2, n));
    }

    private static double validateAndGetRange(double v1, double v2) {
        checkArgument(Double.isFinite(v1), "lower bound must be finite");
        checkArgument(Double.isFinite(v2), "upper bound must be finite");
        checkArgument(v1 <= v2, String.format("v1 must be less than v2 (%s > %s)", v1, v2));
        var range = v2 - v1;
        return (range < 1e-12) ? 1.0 : range;
    }

    private static List<ValueTick> sciTicks(double v1, double v2, int n) {
        return List.of(ValueTick.create(v1, 0.0), ValueTick.create(v2, 0.0));
    }

    private static boolean majorLabelDuplication(List<ValueTick> ticks) {
        var majorTicks = ticks.stream().filter(ValueTick::major).collect(Collectors.toList());
        return majorTicks.size() > majorTicks.stream().map(ValueTick::getLabel).distinct().count();
    }

    private static List<ValueTick> decimalTicks(double v1, double v2, int n, TickSize t, Scale scale) {
        if (Scale.LOGARITHMIC != scale) {
            return normalTicks(v1, v2, t);
        }

        var logDistanceLimit = 2;
        List<ValueTick> finalTicks;

        if (v1 >= 0) {
            // positive range
            var logDistance = logDiff(v1, v2);
            if (logDistance <= logDistanceLimit) {
                return normalTicks(v1, v2, t);
            }
            finalTicks = logScaleTicks(v1, v2, getLogMajorStepSize(logDistance, n));
        } else if (v2 <= 0) {
            // negative range: convert range to pos, generate ticks and convert ticks to negs and reverse
            var logDistance = logDiff(-v2, -v1);
            if (logDistance <= logDistanceLimit) {
                return normalTicks(v1, v2, t);
            }
            finalTicks = toNegTicks(logScaleTicks(-v2, -v1, getLogMajorStepSize(logDistance, n)));
        } else {
            // negative-positive range: split range to pos and neg, get ticks separately and combine
            var posLogDistance = logDiff(0, v2);
            var negLogDistance = logDiff(0, -v1);
            var logDistance = posLogDistance + negLogDistance;
            if (posLogDistance <= logDistanceLimit && negLogDistance <= logDistanceLimit) {
                return normalTicks(v1, v2, t);
            }
            var logMajorStepSize = getLogMajorStepSize(logDistance, n);
            var negTicks = toNegTicks(logScaleTicks(0, -v1, logMajorStepSize));
            var posTicks = logScaleTicks(0, v2, logMajorStepSize);
            // remove the dup 0 tick before combine
            finalTicks = Stream.concat(negTicks.subList(0, negTicks.size() - 1).stream(), posTicks.stream()).collect(Collectors.toList());
        }

        // trim unnecessary ticks
        if (finalTicks.get(0).v() < v1) {
            finalTicks = finalTicks.subList(1, finalTicks.size());
        }
        if (finalTicks.get(finalTicks.size() - 1).v() > v2) {
            finalTicks = finalTicks.subList(0, finalTicks.size() - 1);
        }

        return finalTicks;
    }

    private static List<ValueTick> normalTicks(double v1, double v2, TickSize t) {
        var ticks = ImmutableList.<ValueTick>builder();

        var prefix = getPrefix(Math.abs(v2), t.major);
        var labelFmt = labelFormat(prefix, t.major);

        var base = round(v1, t.major);
        var end = ((int) ((v2 - base) / t.minor)) + 1;
        var pos = 0;
        while (pos <= end) {
            var v = base + pos * t.minor;
            if (v >= v1 && v <= v2) {
                var label = prefix.format(v, labelFmt);
                ticks.add(new ValueTick(v, 0.0, pos % t.minorPerMajor == 0, label));
            }
            pos += 1;
        }
        var ts = ticks.build();

        var useOffset = majorLabelDuplication(ts);
        if (!useOffset) {
            return ts;
        }
        return ts.stream()
                .map(tt -> new ValueTick(tt.v(), base, tt.major(), null))
                .collect(Collectors.toList());
    }

    private static List<ValueTick> toNegTicks(List<ValueTick> ticks) {
        return Lists.reverse(ticks.stream().map(t -> new ValueTick(-1 * t.v(), t.offset(), t.major(), t.label().map(s -> "-" + s).orElse(null))).collect(Collectors.toList()));
    }

    private static int getLogMajorStepSize(int logDistance, int n) {
        // return (logDistance <= n) ? 1 : (int) Math.ceil(logDistance / n);
        return (logDistance <= n) ? 1 : (int) Math.ceil((double) logDistance / n);
    }


    // Note: all below log* functions are assuming values are non-negative
    private static List<ValueTick> logScaleTicks(double v1, double v2, int logMajorStepSize) {
        var min = logFloor(v1);
        var max = logCeil(v2);

        var ticks = ImmutableList.<ValueTick>builder();
        var curr = min;
        while (curr <= max) {
            // show tick for 0 but not 1(10^0) if lower boundary is 0, because they are too close
            // in log scale
            var v = (v1 == 0 && curr == 0) ? 0 : Math.pow(10, curr);
            var label = UnitPrefix.decimal(v).format(v, "%.0f%s");
            ticks.add(new ValueTick(v, 0.0, (curr - min) % logMajorStepSize == 0, label));
            curr += 1;
        }

        return ticks.build();
    }

    private static int logDiff(double v1, double v2) {
        checkArgument(v1 >= 0, "v1 cannot be negative");
        checkArgument(v1 <= v2, "v1 cannot be greater than v2");
        return logCeil(v2) - logFloor(v1);
    }

    private static int logFloor(double v) {
        return (v <= 1) ? 0 : (int) Math.floor(Math.log10(v));
    }

    private static int logCeil(double v) {
        return (int) Math.ceil(Math.log10(v));
    }

    private static List<ValueTick> binaryTicks(double v1, double v2, TickSize t) {
        var ticks = ImmutableList.<ValueTick>builder();

        var prefix = getBinaryPrefix(Math.abs(v2), t.major);
        var labelFmt = binaryLabelFormat(prefix, t.major);

        var base = round(v1, t.major);
        var end = (int) ((v2 - base) / t.minor) + 1;
        var pos = 0;
        while (pos <= end) {
            var v = base + pos * t.minor;
            if (v >= v1 && v <= v2) {
                var label = prefix.format(v, labelFmt);
                ticks.add(new ValueTick(v, 0.0, pos % t.minorPerMajor == 0, label));
            }
            pos += 1;
        }
        var ts = ticks.build();

        if (ts.isEmpty()) {
            return List.of(ValueTick.create(v1, v1), ValueTick.create(v2, v1));
        }
        var useOffset = t.major < Math.abs(v1) / 1e2;
        if (!useOffset) {
            return ts;
        }
        var max = ts.stream().mapToDouble(tt -> tt.v() - base).max().orElse(0);
        var offsetPrefix = getBinaryPrefix(max, max);
        var fmt = binaryLabelFormat(offsetPrefix, max);
        return ts.stream()
                .map(tt -> new ValueTick(tt.v(), base, tt.major(), offsetPrefix.format(tt.v() - base, fmt)))
                .collect(Collectors.toList());
    }

    private static List<ValueTick> durationTicks(
            double v1,
            double v2,
            TickSize t,
            @Nullable UnitPrefix prevPrefix
    ) {
        var ticks = ImmutableList.<ValueTick>builder();
        var prefix = (prevPrefix == null) ? getDurationPrefix(Math.abs(v2), t.major) : prevPrefix;
        var labelFmt = durationLabelFormat(prefix, v2);

        var base = round(v1, t.major);
        var end = (int) ((v2 - base) / t.minor) + 1;
        var pos = 0;
        while (pos <= end) {
            var v = base + pos * t.minor;
            if (v >= v1 && v <= v2) {
                var label = prefix.format(v, labelFmt);
                ticks.add(new ValueTick(v, 0.0, pos % t.minorPerMajor == 0, label));
            }
            pos += 1;
        }
        var ts = ticks.build();

        var useOffset = majorLabelDuplication(ts);
        if (!useOffset) {
            return ts;
        }
        if (prevPrefix == null && v2 < (UnitPrefix.year.factor() * 2) && v2 > 1e-3) {
            var previousPrefix = prefix.prevDurationPrefix();
            return durationTicks(v1, v2, t, previousPrefix);
        }
        var range = v2 - v1;
        var offsetPrefix = getDurationPrefix(range, t.major);
        var newFormat = durationLabelFormat(prefix, t.major);

        return ts.stream()
                .map(tt -> new ValueTick(tt.v(), base, tt.major(), offsetPrefix.format(tt.v() - base, newFormat)))
                .collect(Collectors.toList());
    }

    /**
     * Round to multiple of `s`.
     */
    private static double round(double v, double s) {
        return s * Math.floor(v / s);
    }

    /**
     * Determine the prefix associated with the data. Use the value for the delta between major
     * tick marks if possible. If this will require too many digits to render the largest value, then
     * use the unit prefix found for the largest value.
     */
    private static UnitPrefix getPrefix(double v, double major) {
        var m = UnitPrefix.forRange(major, 3);
        return (v <= 10.0 * m.factor()) ? m : UnitPrefix.forRange(v, 3);
    }

    /**
     * Determine the prefix associated with the data. Use the value for the delta between major
     * tick marks if possible. If this will require too many digits to render the largest value, then
     * use the unit prefix found for the largest value.
     */
    private static UnitPrefix getBinaryPrefix(double v, double major) {
        var m = UnitPrefix.binaryRange(major, 4);
        return (v < 10.0 * m.factor()) ? m : UnitPrefix.binaryRange(v, 4);
    }

    private static UnitPrefix getDurationPrefix(double v, double major) {
        var m = UnitPrefix.durationRange(major);
        return (v <= m.factor()) ? m : UnitPrefix.durationRange(v);
    }

    /**
     * Determines the string format pattern to use for showing the label. This is primarily focused
     * on where the decimal point should go such that:
     * <p>
     * 1. All tick labels will have the decimal point in the same position to make visual scanning
     * easier.
     * 2. Avoid the use of an offset when the number can be shown without an offset by shifting
     * the decimal point.
     */
    private static String labelFormat(UnitPrefix prefix, double v) {
        var f = v / prefix.factor();
        var i = (int) (f * 1000.0);
        if ((i % 10) > 0) {
            return "%.3f%s"; // 1.234
        }
        if ((i % 100) > 0) {
            return "%.2f%s"; // 12.34
        }
        return "%.1f%s"; // 123.4
    }

    /**
     * Determines the string format pattern to use for showing the label. This is primarily focused
     * on where the decimal point should go such that:
     * <p>
     * 1. All tick labels will have the decimal point in the same position to make visual scanning
     * easier.
     * 2. Avoid the use of an offset when the number can be shown without an offset by shifting
     * the decimal point.
     */
    private static String binaryLabelFormat(UnitPrefix prefix, double v) {
        var f = v / prefix.factor();
        var i = (int) (f * 1000.0);
        if ((i % 10) > 0) {
            return "%.2f%s"; // 1.23
        }
        if ((i % 100) > 0) {
            return "%.1f%s"; // 12.3
        }
        return "%.0f%s"; //  123
    }

    private static String durationLabelFormat(UnitPrefix prefix, double v) {
        var f = (v < 1) ? v / prefix.factor() : v;
        var i = (int) (f * 1000.0);
        if (v >= 3.1536e10) {
            return "%.1e%s"; // 1000+ years switch to exponent
        }
        if (v >= 60) {
            return "%.0f%s";
        }
        if ((i % 10) > 0) {
            return "%.3f%s"; // 1.234
        }
        if ((i % 100) > 0) {
            return "%.2f%s"; // 12.34
        }
        return "%.1f%s"; // 123.4
    }

    private static class TickSize {
        private final double major;
        private final double minor;
        private final int minorPerMajor;

        private TickSize(double major, double minor, int minorPerMajor) {
            this.major = major;
            this.minor = minor;
            this.minorPerMajor = minorPerMajor;
        }
    }

    // Major and minor tick sizes for value axis
    private static final List<TickSize> valueTickSizes = valueTickSizes();

    private static List<TickSize> valueTickSizes() {
        return IntStream.rangeClosed(-25, 25).boxed().flatMap(i -> {
            var f = Math.pow(10, i);
            return baseValueTickSizes.stream().map(b -> {
                var minorPerMajor = b.first / b.second;  // Number of minor ticks to use between major ticks
                return new TickSize(b.first * f, b.second * f, minorPerMajor);
            });
        }).collect(Collectors.toList());
    }

    private static final List<TickSize> binaryValueTickSizes = makeBinaryValueTickSizes();

    private static List<TickSize> makeBinaryValueTickSizes() {
        var ltOneKi = IntStream.rangeClosed(-1, 1).boxed().flatMap(i -> {
            var f = Math.pow(10, i);
            return baseValueTickSizes.stream().map(b -> {
                var minorPerMajor = b.first / b.second; // Number of minor ticks to use between major ticks
                return new TickSize(b.first * f, b.second * f, minorPerMajor);
            });
        });

        var majorMultiples = List.of(1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 100, 200, 300, 400, 500);
        var gtOneKi = UnitPrefix.binaryPrefixes.stream().flatMap(prefix -> {
            var n = 4;
            var majorF = prefix.factor();
            var minorF = prefix.factor() / 4.0;
            return majorMultiples.stream().map(m -> new TickSize(majorF * m, minorF * m, n));
        });

        return Stream.concat(ltOneKi, gtOneKi).collect(Collectors.toList());
    }

    private static final List<TickSize> durationValueTickSizes = makeDurationValueTickSizes();

    private static List<TickSize> makeDurationValueTickSizes() {
        var ticks = ImmutableList.<TickSize>builder();
        for (var i = -25; i <= -2; i++) {
            var f = Math.pow(10, i);
            for (var b : baseValueTickSizes) {
                var minorPerMajor = b.first / b.second;
                ticks.add(new TickSize(b.first * f, b.second * f, minorPerMajor));
            }
        }

        var majorMultiples = List.of(
                List.of(1, 2, 3, 4, 5, 6, 10, 15, 30, 60),
                List.of(4, 5, 6, 10, 15, 30, 3600),
                List.of(1, 2, 3, 4, 6, 12, 3600 * 24),
                List.of(1, 2, 4, 6, 12, 24, 86400 * 7),
                List.of(1, 2, 3, 4, 6, 86400 * 365)
        );

        var lastFactor = 0.0;
        for (int i = 1; i < UnitPrefix.durationBigPrefixes.size(); i++) {
            var nextPrefix = Lists.reverse(UnitPrefix.durationBigPrefixes).get(i);
            var n = 4;
            var multiples = majorMultiples.get(i - 1);
            for (var m : Lists.reverse(multiples)) {
                var q = nextPrefix.factor() / m;
                ticks.add(new TickSize(lastFactor + q, q / n, n));
            }
            lastFactor = nextPrefix.factor();
        }

        var mm = List.of(1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 100, 200, 300, 400, 500);
        var n = 4;
        var majorF = UnitPrefix.year.factor();
        var minorF = UnitPrefix.year.factor() / 4.0;
        for (var m : mm) {
            ticks.add(new TickSize(majorF * m, minorF * m, n));
        }

        for (var i = 10; i <= 25; i++) {
            var f = Math.pow(10, i);
            for (var b : baseValueTickSizes) {
                var minorPerMajor = b.first / b.second;
                ticks.add(new TickSize(b.first * f, b.second * f, minorPerMajor));
            }
        }

        return ticks.build();
    }

    private Ticks() {
        // Do not instantiate.
    }
}
