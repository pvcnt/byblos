package dev.byblos.chart.model;

import com.google.common.base.Objects;

/**
 * Sets the bound to the specified value regardless of the data in the chart.
 */
public final class ExplicitBound implements PlotBound {
    private final double value;

    public ExplicitBound(double value) {
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public double lower(boolean hasArea, double min) {
        return value;
    }

    @Override
    public double upper(boolean hasArea, double max) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExplicitBound that = (ExplicitBound) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
