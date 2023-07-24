package dev.byblos.model;

import com.google.common.collect.Multimap;

import java.util.List;

/**
 * Base type for expressions that have a set of time series as the result.
 */
public interface TimeSeriesExpr extends Expr {
    /**
     * Return the underlying data expressions that supply input for the evaluation.
     *
     * These are used to fetch data from the data stores. There may be some expressions
     * types that generate data and will have an empty set. Examples are constants, random,
     * or time.
     */
    List<DataExpr> dataExprs();

    ResultSet eval(EvalContext context, Multimap<DataExpr, TimeSeries> data);
}
