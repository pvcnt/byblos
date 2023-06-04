package dev.byblos.chart.model;

import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;

import static java.util.Objects.requireNonNull;

/**
 * Definition for a vertical span.
 */
public final class VSpanDef implements DataDef {
    private final Instant t1;
    private final Instant t2;
    private final Color color;
    private final String label;

    /**
     * Constructor.
     *
     * @param t1 Starting time for the span.
     * @param t2 Ending time for the span.
     * @param color Color to use when rendering the span.
     * @param label Label associated with the span to use in the legend.
     */
    public VSpanDef(Instant t1, Instant t2, Color color, @Nullable String label) {
        this.t1 = requireNonNull(t1);
        this.t2 = requireNonNull(t2);
        this.color = requireNonNull(color);
        this.label = label;
    }

    public Instant t1() {
        return t1;
    }

    public Instant t2() {
        return t2;
    }

    public String label() {
        if (null != label) {
            return label;
        }
        return String.format("span from %s to %s", t1, t2);
    }

    @Override
    public Color color() {
        return color;
    }

    @Override
    public VSpanDef withColor(Color c) {
        return new VSpanDef(t1, t2, c, label);
    }
}
