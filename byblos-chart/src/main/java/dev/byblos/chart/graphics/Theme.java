package dev.byblos.chart.graphics;

import com.typesafe.config.Config;


/**
 * Theme specifying the styles to use for the chart.
 */
public final class Theme {
    private final Styles image;
    private final Styles canvas;
    private final Styles minorGrid;
    private final Styles majorGrid;
    private final Styles axis;
    private final Styles legend;
    private final Styles warnings;

    /**
     * Constructor.
     *
     * @param image     Styles for the overall image.
     * @param canvas    Styles for the canvas area used to render the data of the chart.
     * @param minorGrid Styles used for rendering the minor grid lines.
     * @param majorGrid Styles used for rendering the major grid lines.
     * @param axis      Styles used for rendering the axes.
     * @param legend    Styles used for rendering the legend entries.
     * @param warnings  Styles used for rendering warning messages.
     */
    public Theme(Styles image, Styles canvas, Styles minorGrid, Styles majorGrid, Styles axis, Styles legend, Styles warnings) {
        this.image = image;
        this.canvas = canvas;
        this.minorGrid = minorGrid;
        this.majorGrid = majorGrid;
        this.axis = axis;
        this.legend = legend;
        this.warnings = warnings;
    }

    public static Theme fromConfig(Config config) {
        return new Theme(
                Styles.fromConfig(config.getConfig("image")),
                Styles.fromConfig(config.getConfig("canvas")),
                Styles.fromConfig(config.getConfig("minor-grid")),
                Styles.fromConfig(config.getConfig("major-grid")),
                Styles.fromConfig(config.getConfig("axis")),
                Styles.fromConfig(config.getConfig("legend")),
                Styles.fromConfig(config.getConfig("warnings"))
        );
    }

    public Styles image() {
        return image;
    }

    public Styles canvas() {
        return canvas;
    }

    public Styles minorGrid() {
        return minorGrid;
    }

    public Styles majorGrid() {
        return majorGrid;
    }

    public Styles axis() {
        return axis;
    }

    public Styles legend() {
        return legend;
    }

    public Styles warnings() {
        return warnings;
    }
}