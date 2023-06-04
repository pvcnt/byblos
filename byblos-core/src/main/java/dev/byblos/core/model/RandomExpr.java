package dev.byblos.core.model;

import com.google.common.collect.Multimap;
import dev.byblos.core.util.Hash;

import java.util.List;

/**
 * Generate a time series that appears to be random noise for the purposes of
 * experimentation and generating sample data. To ensure that the line is deterministic
 * and reproducible it actually is based on a hash of the timestamp.
 */
public final class RandomExpr implements MathExpr {
    @Override
    public ResultSet eval(EvalContext context, Multimap<DataExpr, TimeSeries> data) {
        var seq = new FunctionTimeSeq(context.step(), RandomExpr::rand);
        var ts = MathExpr.newTimeSeries("random", seq);
        return new ResultSet(this, List.of(ts), List.of());
    }

    @Override
    public String toString() {
        return ":random";
    }

    static double rand(long t) {
        // Compute the hash and map the value to the range 0.0 to 1.0.
        return (Math.abs(Hash.lowbias64(t)) % 1000) / 1000.0;
    }
}
