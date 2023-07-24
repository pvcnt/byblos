package dev.byblos.model;

import com.google.common.collect.Multimap;

import java.util.List;

public record ConstantExpr(double value) implements MathExpr {
    @Override
    public ResultSet eval(EvalContext context, Multimap<DataExpr, TimeSeries> data) {
        var seq = new FunctionTimeSeq(context.step(), t -> value);
        var ts = MathExpr.newTimeSeries(String.valueOf(value), seq);
        return new ResultSet(this, List.of(ts), List.of());
    }

    @Override
    public String toString() {
        return String.format("%s,:const", value);
    }
}
