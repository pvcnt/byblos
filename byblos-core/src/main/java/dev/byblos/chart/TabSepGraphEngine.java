package dev.byblos.chart;

/**
 * Simple text output using tab as separators. The content-type is also set to `text/plain` to
 * make it more likely to render locally in a browser rather than treated as a download.
 */
public final class TabSepGraphEngine extends CsvGraphEngine {
    public TabSepGraphEngine() {
        super("txt", "text/plain", "\t");
    }
}