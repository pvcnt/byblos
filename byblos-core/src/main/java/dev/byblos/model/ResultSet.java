package dev.byblos.model;

import java.util.List;

public record ResultSet(TimeSeriesExpr expr, List<TimeSeries> data, List<String> messages) {
}
