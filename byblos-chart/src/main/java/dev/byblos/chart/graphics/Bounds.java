package dev.byblos.chart.graphics;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class Bounds {
    private final double min;
    private final double max;

    public Bounds(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bounds that = (Bounds) o;
        return min == that.min && max == that.max;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(min, max);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("min", min)
                .add("max", max)
                .toString();
    }
}
