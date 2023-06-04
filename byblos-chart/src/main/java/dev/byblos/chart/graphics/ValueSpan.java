package dev.byblos.chart.graphics;

import java.awt.Graphics2D;

/**
 * Draws a horizontal span from `v1` to `v2`.
 */
public final class ValueSpan implements Element {
    private final Style style;
    private final double v1;
    private final double v2;
    private final ValueAxis yaxis;

    /**
     * Constructor.
     *
     * @param style Style to use for filling the span.
     * @param v1    Starting value for the span.
     * @param v2    Ending value for the span.
     * @param yaxis Axis used for creating the scale.
     */
    public ValueSpan(Style style, double v1, double v2, ValueAxis yaxis) {
        this.style = style;
        this.v1 = v1;
        this.v2 = v2;
        this.yaxis = yaxis;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        style.configure(g);
        var yscale = yaxis.scale(y1, y2);
        var p1 = yscale.apply(v1);
        var p2 = yscale.apply(v2);
        var py1 = Math.min(p1, p2);
        var py2 = Math.max(p1, p2);
        g.fillRect(x1, py1, x2 - x1, py2 - py1);
    }
}
