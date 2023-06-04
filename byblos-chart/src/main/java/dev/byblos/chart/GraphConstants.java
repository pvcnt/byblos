package dev.byblos.chart;

import com.netflix.iep.config.ConfigManager;
import com.typesafe.config.Config;

public final class GraphConstants {
    private static final Config config = ConfigManager.load().getConfig("byblos").getConfig("chart").getConfig("limits");

    public static final int MaxYAxis = config.getInt("max-yaxes");
    public static final int MaxLinesInLegend = config.getInt("max-lines-in-legend");
    public static final int MinCanvasWidth = config.getInt("min-canvas-width");
    public static final int MinCanvasHeight = config.getInt("min-canvas-height");
    public static final int MaxWidth = config.getInt("max-width");
    public static final int MaxHeight = config.getInt("max-height");
    public static final double MaxZoom = config.getDouble("max-zoom");

    private GraphConstants() {
        // Do not instantiate.
    }
}
