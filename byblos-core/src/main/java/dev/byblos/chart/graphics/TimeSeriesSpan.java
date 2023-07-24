package dev.byblos.chart.graphics;

import dev.byblos.model.TimeSeq;

import java.awt.Graphics2D;

import static dev.byblos.util.MoreMath.isNearlyZero;

public final class TimeSeriesSpan implements Element {

    private final Style style;
    private final TimeSeq ts;
    private final TimeAxis xaxis;

    public TimeSeriesSpan(Style style, TimeSeq ts, TimeAxis xaxis) {
        this.style = style;
        this.ts = ts;
        this.xaxis = xaxis;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        style.configure(g);
        var step = ts.step();
        var xscale = xaxis.scale(x1, x2);
        var t = xaxis.start();
        while (t < xaxis.end()) {
            var px1 = xscale.apply(t - step);
            var px2 = xscale.apply(t);
            if (!isNearlyZero(ts.get(t))) {
                g.fillRect(px1, y1, px2 - px1, y2 - y1);
            }
            t += step;
        }
    }
}

