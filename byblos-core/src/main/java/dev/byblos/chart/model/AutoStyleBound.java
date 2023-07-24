package dev.byblos.chart.model;

/**
 * Automatically set the bounds so the visual settings for the lines. In particular, if a line
 * style is set to area or stack, then the bounds will be adjusted so it goes to the axis.
 */
public enum AutoStyleBound implements PlotBound {
    INSTANCE;

    @Override
    public String toString() {
      return "auto-style";
    }

    @Override
    public double lower(boolean hasArea, double min) {
        return (hasArea && min > 0.0) ? 0.0 : min;
    }

    @Override
    public double upper(boolean hasArea, double max) {
        return (hasArea && max < 0.0) ? 0.0 : max;
    }
}
