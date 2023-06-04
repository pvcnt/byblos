package dev.byblos.chart.graphics;

import dev.byblos.chart.model.DataDef;
import dev.byblos.chart.model.LineDef;
import dev.byblos.chart.model.MessageDef;
import dev.byblos.chart.model.PlotDef;
import dev.byblos.core.util.UnitPrefix;

import java.awt.Graphics2D;
import java.util.List;

/**
 * Draws a legend entry for a line.
 */
public final class LegendEntry implements Element, FixedHeight {
    private final Styles styles;
    private final PlotDef plot;
    private final DataDef data;
    private final boolean showStats;
    private final boolean shouldShowStats;

    /**
     * Constructor.
     *
     * @param styles    Styles for elements on the legend entry.
     * @param plot      Definition for the plot containing the line.
     * @param data      Definition for the data element.
     * @param showStats If true then summary stats will be shown below the label for the line.
     */
    public LegendEntry(Styles styles, PlotDef plot, DataDef data, boolean showStats) {
        this.styles = styles;
        this.plot = plot;
        this.data = data;
        this.showStats = showStats;
        shouldShowStats = showStats && data instanceof LineDef;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        if (data instanceof MessageDef) {
            // Draw the label
            var txt = Text.left(data.label(), Style.create(data.color()));
            var truncated = txt.truncate(x2 - x1);
            truncated.draw(g, x1, y1, x2, y2);
        } else {
            var d = ChartSettings.normalFontDims.height() - 4;

            // Draw the color box for the legend entry. If the color has an alpha setting, then the
            // background can impact the color so we first fill with the background color of the chart.
            g.setColor(styles.background().color());
            g.fillRect(x1 + 2, y1 + 2, d, d);
            g.setColor(data.color());
            g.fillRect(x1 + 2, y1 + 2, d, d);

            // Border for the color box
            styles.line().configure(g);
            g.drawRect(x1 + 2, y1 + 2, d, d);

            // Draw the label
            var txt = Text.left(data.label(), styles.text());
            var truncated = txt.truncate(x2 - x1 - d - 4);
            truncated.draw(g, x1 + d + 4, y1, x2, y2);

            if (showStats && data instanceof LineDef) {
                var line = (LineDef) data;
                var stats = line.legendStats();
                var max = format(stats.max());
                var min = format(stats.min());
                var avg = format(stats.avg());
                var last = format(stats.last());
                var total = format(stats.total());
                var count = format(stats.count(), false);

                var rows = List.of(
                        String.format("    Max : %-11s   Min  : %-11s", max, min),
                        String.format("    Avg : %-11s   Last : %-11s", avg, last),
                        String.format("    Tot : %-11s   Cnt  : %-11s", total, count)
                );
                var offset = y1 + ChartSettings.normalFontDims.height();
                var rowHeight = ChartSettings.smallFontDims.height();
                for (var i = 0; i < rows.size(); i++) {
                    txt = Text.left(rows.get(i), ChartSettings.smallFont, styles.text());
                    txt.draw(g, x1 + d + 4, offset + i * rowHeight, x2, y2);
                }
            }
        }
    }

    @Override
    public int height() {
        if (!shouldShowStats) {
            return ChartSettings.normalFontDims.height();
        }
        return ChartSettings.normalFontDims.height() + ChartSettings.smallFontDims.height() * 3;
    }

    private String format(double v) {
        return format(v, true);
    }

    private String format(double v, boolean specialPrefix) {
        switch (plot.tickLabelMode()) {
            case BINARY:
                if (specialPrefix) {
                    return UnitPrefix.binary(v).format(v, "%9.2f%1s", "%8.1e ");
                }
                return UnitPrefix.decimal(v).format(v, "%9.2f%1s", "%8.1e ");
            case DURATION:
                if (specialPrefix) {
                    return UnitPrefix.duration(v).format(v, "%9.2f%1s", "%8.1e ");
                }
                return UnitPrefix.decimal(v).format(v, "%9.2f%1s", "%8.1e ");
            default:
                return UnitPrefix.decimal(v).format(v, "%9.3f%1s", "%8.1e ");
        }
    }
}
