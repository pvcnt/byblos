package dev.byblos.eval.backend;

import dev.byblos.core.model.DataExpr;
import dev.byblos.core.model.EvalContext;
import dev.byblos.core.model.TimeSeries;

import java.io.IOException;
import java.util.List;

/**
 * A backend allowing to query for time series.
 */
public interface Backend {
    List<TimeSeries> query(EvalContext context, DataExpr expr) throws IOException;
}
