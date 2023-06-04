package dev.byblos.chart.graphics;

import dev.byblos.core.model.TimeSeq;

import java.awt.Graphics2D;

public final class TimeSeriesStack implements Element {
    /**
     * Stacked offsets for each time interval in the chart.
     */
    public static final class Offsets {
        private final double[] posY;
        private final double[] negY;

        private Offsets(double[] posY, double[] negY) {
            this.posY = posY;
            this.negY = negY;
        }

        public static Offsets fromAxis(TimeAxis axis) {
            var size = (int) ((axis.end() - axis.start()) / axis.step());
            var posY = new double[size];
            var negY = new double[size];
            return new Offsets(posY, negY);
        }
    }

    private final Style style;
    private final TimeSeq ts;
    private final TimeAxis xaxis;
    private final ValueAxis yaxis;

    private final Offsets offsets;

    public TimeSeriesStack(Style style, TimeSeq ts, TimeAxis xaxis, ValueAxis yaxis, Offsets offsets) {
        this.style = style;
        this.ts = ts;
        this.xaxis = xaxis;
        this.yaxis = yaxis;
        this.offsets = offsets;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        var step = xaxis.step();
        var xscale = xaxis.scale(x1, x2);
        var yscale = yaxis.scale(y1, y2);
        var t = xaxis.start();
        while (t < xaxis.end()) {
            var px1 = xscale.apply(t - step);
            var px2 = xscale.apply(t);
            var ox = (int) ((t - xaxis.start()) / step);
            var posY = offsets.posY[ox];
            var negY = offsets.negY[ox];
            style.configure(g);
            var v = ts.get(t);
            if (v == 0.0 && posY == 0.0 && negY == 0.0) {
                // Provides a line along the xaxis to avoid confusion between 0 and NaN (no data)
                var py1 = yscale.apply(posY);
                g.fillRect(px1, py1, px2 - px1, 1);
            }
            if (v > 0.0) {
                var axisy = yscale.apply(posY);
                var py = yscale.apply(v + posY);
                var py1 = Math.min(axisy, py);
                var py2 = Math.max(axisy, py) + 1;
                g.fillRect(px1, py1, px2 - px1, py2 - py1);
                offsets.posY[ox] = v + posY;
            }
            if (v < 0.0) {
                var axisy = yscale.apply(negY);
                var py = yscale.apply(v + negY);
                var py1 = Math.min(axisy, py);
                var py2 = Math.max(axisy, py) + 1;
                g.fillRect(px1, py1, px2 - px1, py2 - py1);
                offsets.negY[ox] = v + negY;
            }
            t += step;
        }
    }
}
