package dev.byblos.core.model;

import com.google.common.collect.Multimap;

import java.time.Duration;
import java.util.List;

public record DataExpr(String exprString, Duration offset) implements TimeSeriesExpr {
    public DataExpr(String exprString) {
        this(exprString, Duration.ZERO);
    }

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
