package dev.byblos.chart.graphics;

import dev.byblos.core.model.TimeSeq;

import java.awt.Graphics2D;

/**
 * Draws a time series as a stepped line.
 */
public final class TimeSeriesLine implements Element {

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
    public TimeSeriesLine(Style style, TimeSeq ts, TimeAxis xaxis, ValueAxis yaxis) {
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
        var t = xaxis.start();
        var pv = ts.get(t);
        while (t < xaxis.end()) {
            var px1 = xscale.apply(t - step);
            var px2 = xscale.apply(t);
            var nv = ts.get(t);
            var py = yscale.apply(pv);
            var ny = yscale.apply(nv);
            if (!Double.isNaN(pv) && !Double.isNaN(nv)) {
                g.drawLine(px1, py, px1, ny);
            }
            if (!Double.isNaN(nv)) {
                g.drawLine(px1, ny, px2, ny);
            }
            t += step;
            pv = nv;
        }
    }
}
