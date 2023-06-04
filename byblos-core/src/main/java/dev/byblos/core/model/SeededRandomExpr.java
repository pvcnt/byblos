package dev.byblos.core.model;

import com.google.common.collect.Multimap;

import java.util.List;

/**
 * Same as {@link RandomExpr}, but allows the user to specify a seed to vary the input. This allows
 * multiple sample lines to be produced with different values.
 */
public final class SeededRandomExpr implements MathExpr {
    private final int seed;

    public SeededRandomExpr(int seed) {
        this.seed = seed;
    }

    @Override
    public ResultSet eval(EvalContext context, Multimap<DataExpr, TimeSeries> data) {
        var seq = new FunctionTimeSeq(context.step(), this::rand);
        var label = String.format("seeded-random(%s)", seed);
        var ts = MathExpr.newTimeSeries(label, seq);
        return new ResultSet(this, List.of(ts), List.of());
    }

    @Override
    public String toString() {
        return seed + ",:random";
    }

    private double rand(long t) {
        return RandomExpr.rand(t ^ seed);
    }
}
