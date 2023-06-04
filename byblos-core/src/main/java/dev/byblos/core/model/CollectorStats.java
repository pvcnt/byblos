package dev.byblos.core.model;

/**
 * Summary stats for how much data was processed by a collector.
 */
public final class CollectorStats {

    private final long inputLines;
    private final long inputDatapoints;
    private final long outputLines;
    private final long outputDatapoints;

    /**
     * Constructor.
     *
     * @param inputLines       number of lines in the input to the collector
     * @param inputDatapoints  number of datapoints in the input to the collector
     * @param outputLines      number of lines in the result output
     * @param outputDatapoints number of datapoints in the result output
     */
    public CollectorStats(long inputLines, long inputDatapoints, long outputLines, long outputDatapoints) {
        this.inputLines = inputLines;
        this.inputDatapoints = inputDatapoints;
        this.outputLines = outputLines;
        this.outputDatapoints = outputDatapoints;
    }

    public static final CollectorStats EMPTY = new CollectorStats(0, 0, 0, 0);

    public long inputLines() {
        return inputLines;
    }

    public long inputDatapoints() {
        return inputDatapoints;
    }

    public long outputLines() {
        return outputLines;
    }

    public long outputDatapoints() {
        return outputDatapoints;
    }
}
