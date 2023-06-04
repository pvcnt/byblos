package dev.byblos.chart.model;

import java.awt.*;

/**
 * Defines data to show in the graph.
 */
public interface DataDef {
    String label();

    Color color();

    DataDef withColor(Color c);
}
