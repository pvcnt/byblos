package dev.byblos.chart.model;

/**
 * Upper or lower bound to use for an axis.
 */
public interface PlotBound {

    double lower(boolean hasArea, double min);

    double upper(boolean hasArea, double max);

    /**
     * Create a bound from a string representation. Acceptable values are: `auto-style`,
     * `auto-data`, or a floating point number.
     */
    static PlotBound fromString(String s) {
        if ("auto-style".equals(s)) {
            return AutoStyleBound.INSTANCE;
        }
        if ("auto-data".equals(s)) {
            return AutoDataBound.INSTANCE;
        }
        return new ExplicitBound(Double.parseDouble(s));
    }
}