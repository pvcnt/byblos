package dev.byblos.chart.graphics;

import dev.byblos.util.UnitPrefix;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Tick mark for the value axis.
 */
public final class ValueTick {
    private final double v;
    private final double offset;
    private final boolean major;
    private final String label;

    /**
     * Constructor.
     *
     * @param v      Value to place the tick mark.
     * @param offset Offset that will be displayed separately. In some cases if there is a large base value with
     *               a small range of values there will be too many significant digits to show in the tick label
     *               of the axis. If the offset is non-zero, then it should be shown separately and the tick
     *               label should be the difference between the value and the offset.
     * @param major  True if the position is a major tick mark.
     * @param label  Label to use for the tick mark. If set to None then a default will be generated using `UnitPrefix`.
     */
    public ValueTick(double v, double offset, boolean major, @Nullable String label) {
        this.v = v;
        this.offset = offset;
        this.major = major;
        this.label = label;
    }

    public static ValueTick create(double v, double offset) {
        return new ValueTick(v, offset, true, null);
    }

    public double v() {
        return v;
    }

    public double offset() {
        return offset;
    }

    public boolean major() {
        return major;
    }

    public Optional<String> label() {
        return Optional.ofNullable(label);
    }

    public String getLabel() {
        return label().orElseGet(() -> UnitPrefix.decimal(v - offset).format(v - offset));
    }
}
