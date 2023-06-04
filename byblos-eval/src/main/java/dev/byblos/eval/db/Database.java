package dev.byblos.eval.db;

import dev.byblos.core.model.DataExpr;
import dev.byblos.core.model.EvalContext;
import dev.byblos.core.model.TimeSeries;

import java.io.IOException;
import java.util.List;

public interface Database {
    List<TimeSeries> execute(EvalContext context, DataExpr expr) throws IOException;
}
