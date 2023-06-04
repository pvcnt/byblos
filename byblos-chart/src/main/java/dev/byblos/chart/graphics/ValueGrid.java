package dev.byblos.chart.graphics;


import java.awt.Graphics2D;

/**
 * Draws horizontal grid lines based on a value axis.
 */
public final class ValueGrid implements Element {

    private final ValueAxis yaxis;
    private final Style major;
    private final Style minor;

    /**
     * Constructor.
     *
     * @param yaxis
     *     Axis to use for creating the scale and determining the the tick marks that correspond with
     *     the major grid lines.
     * @param major
     *     Style to use for drawing the major tick lines.
     * @param minor
     *     Style to use for drawing the minor tick lines.
     */
    public ValueGrid(ValueAxis yaxis, Style major, Style minor) {
        this.yaxis = yaxis;
        this.major = major;
        this.minor = minor;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        var yscale = yaxis.scale(y1, y2);
        var ticks = yaxis.ticks(y1, y2);

        for (var tick: ticks) {
            if (tick.major()) {
                major.configure(g);
            } else {
                minor.configure(g);
            }
            var py = yscale.apply(tick.v());
            if (py != y1 && py != y2) {
                g.drawLine(x1, py, x2, py);
            }
        }
    }
}
