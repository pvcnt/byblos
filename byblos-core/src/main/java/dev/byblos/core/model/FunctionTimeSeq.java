package dev.byblos.core.model;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class FunctionTimeSeq implements TimeSeq {
    private final long step;
    private final Function<Long, Double> f;

    public FunctionTimeSeq(long step, Function<Long, Double> f) {
        this.step = step;
        this.f = requireNonNull(f);
    }

    @Override
    public long step() {
        return step;
    }

    @Override
    public double get(long timestamp) {
        return f.apply(timestamp / step * step);
    }
}
