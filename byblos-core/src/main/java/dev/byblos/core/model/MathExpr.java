package dev.byblos.core.model;

import java.util.List;
import java.util.Map;

public interface MathExpr extends TimeSeriesExpr {
    @Override
    default List<DataExpr> dataExprs() {
        return List.of();
    }

    static TimeSeries newTimeSeries(String name, TimeSeq data) {
        return new TimeSeries(data, name, Map.of(TagKey.name, name));
    }
}
