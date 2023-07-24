package dev.byblos.util;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.LongStream;

/**
 * Utilities for computing and rounding times based on the step size for a dataset.
 */
public final class Step {
    private final static long oneSecond = 1000L;
    private final static long oneMinute = 60000L;
    private final static long oneHour = 60 * oneMinute;
    private final static long oneDay = 24 * oneHour;

    private final static List<Long> allowedStepSizes = makeAllowedStepSizes();

    private static List<Long> makeAllowedStepSizes() {
        ImmutableList.Builder<Long> builder = ImmutableList.builder();
        List<Long> div60 = List.of(1L, 2L, 3L, 4L, 5L, 6L, 10L, 12L, 15L, 20L, 30L);
        div60.stream().mapToLong(x -> x * oneSecond).forEach(builder::add);
        div60.stream().mapToLong(x -> x * oneMinute).forEach(builder::add);
        LongStream.of(1, 2, 3, 6, 8, 12).map(x -> x * oneHour).forEach(builder::add);
        return builder.build();
    }

    private final static List<Long> autoStepSizes = makeAutoStepSizes();

    private static List<Long> makeAutoStepSizes() {
        ImmutableList.Builder<Long> builder = ImmutableList.builder();
        List<Long> div60 = List.of(1L, 5L, 10L, 20L, 30L);
        div60.stream().mapToLong(x -> x * oneSecond).forEach(builder::add);
        div60.stream().mapToLong(x -> x * oneMinute).forEach(builder::add);
        LongStream.of(1, 6, 12).map(x -> x * oneHour).forEach(builder::add);
        return builder.build();
    }

    /**
     * Round an arbitrary step to the next largest allowed step size.
     */
    public static long round(long primary, long step) {
        var max = Math.max(primary, step);
        return allowedStepSizes.stream()
                .filter(x -> x >= max)
                .findFirst()
                .orElseGet(() -> roundToDayBoundary(step));
    }

    /**
     * Compute an appropriate step size so that each datapoint shown for the graph has at least one
     * pixel. The computed step must be a multiple of the primary step that is passed in.
     *
     * @param primary step size that the input data is stored with
     * @param width   width in pixels available for rendering the line
     * @param start   start time for the graph
     * @param end     end time for the graph
     */
    public static long compute(long primary, int width, long start, long end) {
        var datapoints = (end - start) / primary;
        var minStep = datapointsPerPixel(datapoints, width) * primary;
        var max = Math.max(primary, minStep);
        return autoStepSizes.stream()
                .filter(x -> x >= max)
                .findFirst()
                .orElseGet(() -> roundToDayBoundary(max));
    }

    private static long datapointsPerPixel(long datapoints, int width) {
        var v = datapoints / width;
        return (datapoints % width == 0) ? v : v + 1;
    }

    private static long roundToDayBoundary(long step) {
        if (step % oneDay == 0) {
            return step;
        }
        return step / oneDay * oneDay + oneDay;
    }

    private Step() {
        // Do not instantiate.
    }
}