package dev.byblos.chart.model;

public final class PlotBounds {

    /**
     * Create a bound from a string representation. Acceptable values are: `auto-style`,
     * `auto-data`, or a floating point number.
     */
    public static PlotBound fromString(String s) {
        if ("auto-style".equals(s)) {
            return AutoStyleBound.INSTANCE;
        }
        if ("auto-data".equals(s)) {
            return AutoDataBound.INSTANCE;
        }
        return new ExplicitBound(Double.parseDouble(s));
    }

    private PlotBounds() {

    }
}
