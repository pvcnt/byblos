package dev.byblos.chart.graphics;

import dev.byblos.chart.Colors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Graphics2D;

/**
 * Style attributes associated with elements.
 */
public final class Style {
    private final Color color;
    private final Stroke stroke;

    /**
     * Constructor.
     *
     * @param color  Color to set on the graphics object. Default: BLACK
     * @param stroke Stroke to set on the graphics object. Default: BasicStroke(1.0f)
     */
    public Style(Color color, Stroke stroke) {
        this.color = color;
        this.stroke = stroke;
    }

    public static Style create(Color color) {
        return new Style(color, new BasicStroke(1.0f));
    }

    public Color color() {
        return color;
    }

    public void configure(Graphics2D g) {
        g.setColor(color);
        g.setStroke(stroke);
    }

    public Style withAlpha(int alpha) {
        return withColor(Colors.withAlpha(color, alpha));
    }

    public Style withColor(Color c) {
        return new Style(c, stroke);
    }
}
