package dev.byblos.chart.model;

/**
 * Automatically set the bounds using the min and max values for the lines.
 */
public enum AutoDataBound implements PlotBound {
    INSTANCE;

    @Override
    public double lower(boolean hasArea, double min) {
        return min;
    }

    @Override
    public double upper(boolean hasArea, double max) {
        return max;
    }

    @Override
    public String toString() {
        return "auto-data";
    }
}
