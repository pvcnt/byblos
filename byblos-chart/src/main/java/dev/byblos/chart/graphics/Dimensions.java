package dev.byblos.chart.graphics;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class Dimensions {
    private final int width;
    private final int height;

    public Dimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimensions that = (Dimensions) o;
        return width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(width, height);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("width", width)
                .add("height", height)
                .toString();
    }
}
