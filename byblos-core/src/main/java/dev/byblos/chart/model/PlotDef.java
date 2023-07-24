package dev.byblos.chart.model;

import dev.byblos.chart.graphics.Bounds;
import dev.byblos.chart.graphics.Theme;
import org.immutables.value.Value;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Definition for a plot, i.e., a y-axis and associated data elements.
 */
@Value.Immutable
@Value.Style(overshadowImplementation = true)
public abstract class PlotDef {
    @Value.Check
    void check() {
        if (lower() instanceof ExplicitBound && upper() instanceof ExplicitBound) {
            double l = ((ExplicitBound) lower()).value();
            double u = ((ExplicitBound) upper()).value();
            checkArgument(l < u, String.format("lower bound must be less than upper bound (%s >= %s)", l, u));
        }
    }

    /**
     * Return the list of data items to include in the plot.
     */
    public abstract List<DataDef> data();

    /**
     * Return the label to show for the axis.
     */
    public abstract Optional<String> yLabel();

    /**
     * Return the color to use when rendering the axis.
     */
    public abstract Optional<Color> axisColor();

    /**
     * Return the type of scale to use on the axis, linear or logarithmic.
     */
    @Value.Default
    public Scale scale() {
        return Scale.LINEAR;
    }

    /**
     * Return the upper limit for the axis.
     */
    @Value.Default
    public PlotBound upper() {
        return AutoStyleBound.INSTANCE;
    }

    /**
     * Return the lower limit for the axis.
     */
    @Value.Default
    public PlotBound lower() {
        return AutoStyleBound.INSTANCE;
    }

    /**
     * Return the mode to use for displaying tick labels.
     */
    @Value.Default
    public TickLabelMode tickLabelMode() {
        return TickLabelMode.DECIMAL;
    }

    public ImmutablePlotDef.Builder toBuilder() {
        return ImmutablePlotDef.builder().from(this);
    }

    public Bounds bounds(long start, long end) {
        var dataLines = lines();
        if (dataLines.isEmpty()) {
            return new Bounds(0d, 1d);
        }
        var step = dataLines.get(0).data().data().step();
        var regular = dataLines.stream()
                .filter(d -> d.lineStyle() != LineStyle.VSPAN && d.lineStyle() != LineStyle.STACK)
                .collect(Collectors.toList());
        var stacked = dataLines.stream()
                .filter(d -> d.lineStyle() == LineStyle.STACK)
                .collect(Collectors.toList());
        var max = -Double.MAX_VALUE;
        var min = Double.MAX_VALUE;
        var posSum = 0.0;
        var negSum = 0.0;

        var t = start;
        while (t < end) {
            for (var line : regular) {
                var v = line.data().data().get(t);
                if (Double.isFinite(v)) {
                    max = Math.max(v, max);
                    min = Math.min(v, min);
                }
            }

            for (var line : stacked) {
                var v = line.data().data().get(t);
                if (Double.isFinite(v)) {
                    if (v >= 0.0) {
                        posSum += v;
                    } else {
                        negSum += v;
                    }
                }
            }

            if (!stacked.isEmpty()) {
                var v = stacked.get(0).data().data().get(t);
                if (Double.isFinite(v)) {
                    max = Math.max(v, max);
                    min = Math.min(v, min);
                }
                max = (posSum > 0.0 && posSum > max) ? posSum : max;
                min = (negSum < 0.0 && negSum < min) ? negSum : min;
            }
            posSum = 0.0;
            negSum = 0.0;
            t += step;
        }

        // If an area or stack is shown it will fill to zero and the filled area should be shown
        var hasArea = dataLines.stream().anyMatch(line -> line.lineStyle() == LineStyle.AREA || line.lineStyle() == LineStyle.STACK);

        min = (min == Double.MAX_VALUE) ? 0.0 : min;
        max = (max == -Double.MAX_VALUE) ? 1.0 : max;
        return finalBounds(hasArea, min, max);
    }

    public PlotDef normalize(Theme theme) {
        return ImmutablePlotDef.builder().axisColor(getAxisColor(theme.legend().text().color())).build();
    }

    private Bounds finalBounds(boolean hasArea, double min, double max) {
        // Try to figure out bounds following the guidelines:
        // * An explicit bound should always get used.
        // * If an area is present, then automatic bounds should go to the 0 line.
        // * If an automatic bound equals or is on the wrong side of an explicit bound, then pad by 1.
        var l = lower().lower(hasArea, min);
        var u = upper().upper(hasArea, max);

        // If upper and lower bounds are equal or automatic/explicit combination causes lower to be
        // greater than the upper, then pad automatic bounds by 1. Explicit bounds should
        // be honored.
        if (l < u) {
            return new Bounds(l, u);
        }
        if (lower() instanceof ExplicitBound && upper() instanceof ExplicitBound) {
            return new Bounds(l, u);
        }
        if (upper() instanceof ExplicitBound) {
            return new Bounds(u - 1, u);
        }
        if (lower() instanceof ExplicitBound) {
            return new Bounds(l, l + 1);
        }
        return new Bounds(l, u + 1);
    }

    public Color getAxisColor(Color defaultColor) {
        return axisColor().orElse(defaultColor);
    }

    public boolean showTickLabels() {
        return tickLabelMode() != TickLabelMode.OFF;
    }

    public List<HSpanDef> horizontalSpans() {
        return getData(HSpanDef.class);
    }

    public List<VSpanDef> verticalSpans() {
        return getData(VSpanDef.class);
    }

    public List<LineDef> lines() {
        return getData(LineDef.class);
    }

    private <T extends DataDef> List<T> getData(Class<T> clazz) {
        return data().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }
}