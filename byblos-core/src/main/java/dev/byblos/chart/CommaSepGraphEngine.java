package dev.byblos.chart;

/**
 * Simple text output using comma as separators.
 */
public final class CommaSepGraphEngine extends CsvGraphEngine {
    public CommaSepGraphEngine() {
        super("csv", "text/csv", ",");
    }
}
