package dev.byblos.chart.graphics;

import dev.byblos.chart.model.PlotDef;
import dev.byblos.core.util.UnitPrefix;

import java.awt.*;
import java.util.List;

public abstract class ValueAxis implements Element, FixedWidth {
    protected final PlotDef plotDef;
    protected final Style style;
    protected final Text label;
    private final double angle;
    private final double min;
    private final double max;
    protected final int labelHeight = ChartSettings.normalFontDims.height();

    /**
     * Width of value tick labels. The assumption is a monospace font with 7 characters. The 7 is
     * for:
     * <p>
     * - `[sign][3digits][decimal point][1digit][suffix]`: e.g., `-102.3K`
     * - `-1.0e-5`
     */
    private final int tickLabelWidth = ChartSettings.smallFontDims.width() * 7;

    protected final int tickMarkLength = 4;

    private final int minTickLabelHeight = ChartSettings.smallFontDims.height() * 3;

    ValueAxis(PlotDef plotDef, Styles styles, double angle, double min, double max) {
        this.plotDef = plotDef;
        this.angle = angle;
        this.min = min;
        this.max = max;
        var axisColor = plotDef.getAxisColor(styles.line().color());
        style = styles.line().withColor(axisColor);
        label = plotDef.yLabel().map(str -> Text.center(str, style)).orElse(null);
    }


    @Override
    public final int width() {
        return labelHeight + tickLabelWidth + tickMarkLength + 1;
    }

    protected Scales.DoubleFactory valueScale() {
        return Scales.factory(plotDef.scale());
    }

    protected Scales.DoubleScale scale(int y1, int y2) {
        return valueScale().apply(min, max, y1, y2);
    }

    protected List<ValueTick> ticks(int y1, int y2) {
        var numTicks = (y2 - y1) / minTickLabelHeight;
        switch (plotDef.tickLabelMode()) {
            case BINARY:
                return Ticks.binary(min, max, numTicks);
            case DURATION:
                return Ticks.duration(min, max, numTicks);
            default:
                return Ticks.value(min, max, numTicks, plotDef.scale());
        }
    }

    protected void drawLabel(Text text, Graphics2D g, int x1, int y1, int x2, int y2) {
        var transform = g.getTransform();
        var centerX = (x2 - x1) / 2 + x1;
        var centerY = (y2 - y1) / 2 + y1;

        var width = y2 - y1;
        var truncated = text.truncate(width);
        var height = truncated.computeHeight(g, width);
        g.rotate(angle, centerX, centerY);
        truncated.draw(
                g,
                centerX - width / 2,
                centerY - height / 2,
                centerX + width / 2,
                centerY + height / 2
        );
        g.setTransform(transform);
    }

    protected UnitPrefix tickPrefix(double v) {
        switch (plotDef.tickLabelMode()) {
            case OFF:
                return UnitPrefix.one;
            case DECIMAL:
                return UnitPrefix.decimal(v);
            case BINARY:
                return UnitPrefix.binary(v);
            case DURATION:
                return UnitPrefix.duration(v);
            default:
                throw new AssertionError();
        }
    }

    protected String tickLabelFmt() {
        switch (plotDef.tickLabelMode()) {
            case OFF:
                return "";
            case DECIMAL:
                return "%.1f%s";
            case BINARY:
                return "%.0f%s";
            case DURATION:
                return "%.1f%s";
            default:
                throw new AssertionError();
        }
    }
}