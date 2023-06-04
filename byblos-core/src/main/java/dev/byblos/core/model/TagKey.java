package dev.byblos.core.model;

/**
 * Helper functions and constants for standard tag keys.
 */
public final class TagKey {
    private final static String byblosPrefix = "byblos.";

    public final static String name = "__name__";
    public final static String scope = "__scope__";

    /**
     * Synthetic tag that is created for lines in a graph to indicate the time offset the line is
     * shifted by.
     */
    public final static String offset = byblosPrefix + "offset";

    /**
     * Synthetic tag that is created for lines in a graph to substitute the average statistic
     * for the line.
     */
    public final static String avg = byblosPrefix + "avg";

    /**
     * Synthetic tag that is created for lines in a graph to substitute the max statistic
     * for the line.
     */
    public final static String max = byblosPrefix + "max";

    /**
     * Synthetic tag that is created for lines in a graph to substitute the min statistic
     * for the line.
     */
    public final static String min = byblosPrefix + "min";

    /**
     * Synthetic tag that is created for lines in a graph to substitute the last statistic
     * for the line.
     */
    public final static String last = byblosPrefix + "last";

    /**
     * Synthetic tag that is created for lines in a graph to substitute the total statistic
     * for the line.
     */
    public final static String total = byblosPrefix + "total";

    private TagKey() {
        // Do not instantiate.
    }
}
