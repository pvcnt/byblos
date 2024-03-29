package dev.byblos.model;

import com.google.common.collect.Multimap;

import java.util.List;

public record DataExpr(String exprString) implements TimeSeriesExpr {

    @Override
    public List<DataExpr> dataExprs() {
        return List.of(this);
    }

    @Override
    public ResultSet eval(EvalContext context, Multimap<DataExpr, TimeSeries> data) {
        return new ResultSet(this, List.copyOf(data.get(this)), List.of());
    }

    @Override
    public String toString() {
        return exprString();
    }
}
