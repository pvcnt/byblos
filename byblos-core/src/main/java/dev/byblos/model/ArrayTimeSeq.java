package dev.byblos.model;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

public final class ArrayTimeSeq implements TimeSeq {
    private final long start;
    private final long end;
    private final long step;
    private final double[] data;

    public ArrayTimeSeq(long start, long step, double[] data) {
        checkArgument(start % step == 0, "start time must be on step boundary");
        this.start = start;
        end = start + data.length * step;
        this.step = step;
        this.data = data;
    }

    @Override
    public long step() {
        return step;
    }

    @Override
    public double get(long timestamp) {
        var i = (timestamp - start) / step;
        if (timestamp < start || timestamp >= end) {
            return Double.NaN;
        }
        return data[(int) i];
    }

    public double[] data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayTimeSeq that = (ArrayTimeSeq) o;

        if (start != that.start) return false;
        if (step != that.step) return false;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = (int) (start ^ (start >>> 32));
        result = 31 * result + (int) (step ^ (step >>> 32));
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
