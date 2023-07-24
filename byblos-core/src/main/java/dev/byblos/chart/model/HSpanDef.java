package dev.byblos.chart.model;

import javax.annotation.Nullable;
import java.awt.Color;

/**
 * Definition for a horizontal span.
 */
public final class HSpanDef implements DataDef {
    private final double v1;
    private final double v2;
    private final Color color;
    private final String label;

    /**
     * Constructor.
     *
     * @param v1 Starting value for the span.
     * @param v2 Ending value for the span.
     * @param color Color to use when rendering the span.
     * @param label Label associated with the span to use in the legend.
     */
    public HSpanDef(double v1, double v2, Color color, @Nullable String label) {
        this.v1 = v1;
        this.v2 = v2;
        this.color = color;
        this.label = label;
    }

    public double v1() {
        return v1;
    }

    public double v2() {
        return v2;
    }

    public String label() {
        if (null != label) {
            return label;
        }
        return String.format("span from %s to %s", v1, v2);
    }

    @Override
    public Color color() {
        return color;
    }
}
