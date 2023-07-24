package dev.byblos.chart.graphics;


import java.awt.Graphics2D;

/**
 * Draws vertical grid lines based on a time axis.
 */
public final class TimeGrid implements Element {
    private final TimeAxis xaxis;
    private final Style major;
    private final Style minor;

    /**
     * Constructor.
     *
     * @param xaxis Axis to use for creating the scale and determining the the tick marks that correspond with
     *              the major grid lines.
     * @param major Style to use for drawing the major tick lines.
     * @param minor Style to use for drawing the minor tick lines.
     */
    public TimeGrid(TimeAxis xaxis, Style major, Style minor) {
        this.xaxis = xaxis;
        this.major = major;
        this.minor = minor;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        var ticks = xaxis.ticks(x1, x2);
        var xscale = xaxis.scale(x1, x2);

        for (var tick : ticks) {
            if (tick.major()) {
                major.configure(g);
            } else {
                minor.configure(g);
            }
            var px = xscale.apply(tick.timestamp());
            if (px > x1 && px < x2) {
                g.drawLine(px, y1, px, y2);
            }
        }
    }
}
