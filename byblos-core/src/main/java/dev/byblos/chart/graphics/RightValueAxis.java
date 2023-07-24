package dev.byblos.chart.graphics;

import dev.byblos.chart.model.PlotDef;

import java.awt.Graphics2D;
import java.util.List;
import java.util.stream.Collectors;

public final class RightValueAxis extends ValueAxis {
    public RightValueAxis(PlotDef plotDef, Styles styles, double min, double max) {
        super(plotDef, styles, Math.PI / 2.0, min, max);
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        style.configure(g);
        g.drawLine(x1, y1, x1, y2);

        var majorTicks = ticks(y1, y2).stream().filter(ValueTick::major).collect(Collectors.toList());
        var offset = majorTicks.isEmpty() ? 0.0 : majorTicks.get(0).offset();
        if (offset == 0.0 || !plotDef.showTickLabels()) {
            drawNormal(majorTicks, g, x1, y1, x2, y2);
        } else {
            drawWithOffset(majorTicks, g, x1, y1, x2, y2);
        }

        if (null != label) {
            drawLabel(label, g, x2 - labelHeight, y1, x2, y2);
        }
    }

    private void drawNormal(List<ValueTick> ticks, Graphics2D g, int x1, int y1, int x2, int y2) {
        var yscale = scale(y1, y2);
        for (var tick: ticks) {
            var py = yscale.apply(tick.v());
            g.drawLine(x1, py, x1 + tickMarkLength, py);

            if (plotDef.showTickLabels()) {
                var txt = Text.left(tick.getLabel(), ChartSettings.smallFont, style);
                var txtH = ChartSettings.smallFontDims.height();
                var ty = py - txtH / 2;
                txt.draw(g, x1 + tickMarkLength + 1, ty, x2, ty + txtH);
            }
        }
    }

    private void drawWithOffset(List<ValueTick> ticks, Graphics2D g, int x1, int y1, int x2, int y2) {
        var offset = ticks.get(0).v();
        var prefix = tickPrefix(ticks.get(ticks.size() - 1).v() - offset);
        var offsetStr = prefix.format(offset, tickLabelFmt());
        var offsetTxt = Text.right(offsetStr, ChartSettings.smallFont, style);
        var offsetH = ChartSettings.smallFontDims.height() * 2;
        var offsetW = ChartSettings.smallFontDims.width() * (offsetStr.length() + 3);

        var yscale = scale(y1, y2);
        var oy = yscale.apply(offset);
        var otop = oy - offsetW - tickMarkLength;
        var obottom = oy - tickMarkLength;
        drawLabel(offsetTxt, g, x1 + tickMarkLength, otop, x1 + offsetH + tickMarkLength, obottom);
        g.drawLine(x1 + offsetH + tickMarkLength, oy, x1, oy);

        ticks.stream().skip(1).forEach(tick -> {
            var py = yscale.apply(tick.v());
            g.drawLine(x1, py, x1 + tickMarkLength, py);

            if (plotDef.showTickLabels()) {
                var label = "+" + prefix.format(tick.v() - offset, tickLabelFmt());
                var txt = Text.left(label, ChartSettings.smallFont, style);
                var txtH = ChartSettings.smallFontDims.height();
                var ty = py - txtH / 2;
                if (ty + txtH < otop) {
                    txt.draw(g, x1 + tickMarkLength + 1, ty, x2, ty + txtH);
                }
            }
        });
    }
}
