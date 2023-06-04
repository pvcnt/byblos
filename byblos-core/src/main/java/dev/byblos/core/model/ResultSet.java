package dev.byblos.core.model;

import java.util.List;

public record ResultSet(TimeSeriesExpr expr, List<TimeSeries> data, List<String> messages) {
}
