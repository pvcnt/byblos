package dev.byblos.chart.graphics;

import dev.byblos.model.TimeSeq;

import java.awt.Graphics2D;

/**
 * Draws a time series as an area filled to zero. If values are positive the fill will be down to
 * zero. If values are negative the fill will be up to zero.
 */
public final class TimeSeriesArea implements Element {
    private final Style style;
    private final TimeSeq ts;
    private final TimeAxis xaxis;
    private final ValueAxis yaxis;

    /**
     * Constructor.
     *
     * @param style Style to use for drawing the area.
     * @param ts    Data for the line.
     * @param xaxis Axis used to create the X scale.
     * @param yaxis Axis used to create the Y scale.
     */
    public TimeSeriesArea(Style style, TimeSeq ts, TimeAxis xaxis, ValueAxis yaxis) {
        this.style = style;
        this.ts = ts;
        this.xaxis = xaxis;
        this.yaxis = yaxis;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        style.configure(g);
        var step = ts.step();
        var xscale = xaxis.scale(x1, x2);
        var yscale = yaxis.scale(y1, y2);
        var axisy = yscale.apply(0.0);
        var t = xaxis.start();
        while (t < xaxis.end()) {
            var px1 = xscale.apply(t - step);
            var px2 = xscale.apply(t);
            var nv = ts.get(t);
            var ny = yscale.apply(nv);
            var py1 = Math.min(axisy, ny);
            var py2 = Math.max(axisy, ny) + 1;
            if (!Double.isNaN(nv)) {
                g.fillRect(px1, py1, px2 - px1, py2 - py1);
            }
            t += step;
        }
    }
}
