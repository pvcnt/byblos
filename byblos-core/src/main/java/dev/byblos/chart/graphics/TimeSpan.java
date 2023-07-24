package dev.byblos.chart.graphics;

import java.awt.Graphics2D;

/**
 * Draws a vertical span from `t1` to `t2`.
 */
public final class TimeSpan implements Element {
    private final Style style;
    private final long t1;
    private final long t2;
    private final TimeAxis xaxis;

    /**
     * Constructor.
     *
     * @param style Style to use for filling the span.
     * @param t1    Start time in milliseconds since the epoch.
     * @param t2    End time in milliseconds since the epoch.
     *              Step size in milliseconds.
     * @param xaxis Axis used for creating the scale.
     */
    public TimeSpan(Style style, long t1, long t2, TimeAxis xaxis) {
        this.style = style;
        this.t1 = t1;
        this.t2 = t2;
        this.xaxis = xaxis;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        style.configure(g);
        var xscale = xaxis.scale(x1, x2);
        var px1 = xscale.apply(t1);
        var px2 = xscale.apply(t2);
        g.fillRect(px1, y1, px2 - px1, y2 - y1);
    }
}
