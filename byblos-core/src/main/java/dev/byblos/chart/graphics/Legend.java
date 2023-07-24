package dev.byblos.chart.graphics;

import dev.byblos.chart.model.PlotDef;

import javax.annotation.Nullable;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;


/**
 * Draws a legend for a given plot.
 */
public final class Legend implements Element, VariableHeight {
    private final Block block;

    /**
     * Constructor.
     *
     * @param plot Plot definition corresponding to the legend.
     * @param label Overall label to show for this legend.
     * @param showStats Whether to show basic line statistics for the legend entries.
     * @param maxEntries Maximum number of entries to show in the legend.
     */
    public Legend(Styles styles, PlotDef plot, @Nullable String label, boolean showStats, int maxEntries) {
        // Header.
        List<Element> elements = new ArrayList<>();
        elements.add(new HorizontalPadding(5));
        if (null != label) {
            var bold = ChartSettings.normalFont.deriveFont(Font.BOLD);
            var headerColor = plot.getAxisColor(styles.text().color());
            elements.add(Text.left(label, bold, Style.create(headerColor)));
        }

        // Entries.
        plot.data().stream().limit(maxEntries).forEach(data -> {
            elements.add(new HorizontalPadding(2));
            elements.add(new LegendEntry(styles, plot, data, showStats));
        });

        // Footer.
        var numEntries = plot.data().size();
        if (numEntries > maxEntries) {
            var remaining = numEntries - maxEntries;
            elements.add(new HorizontalPadding(2));
            elements.add(Text.left(String.format("... %s suppressed ...", remaining), styles.text()));
        }

        block = new Block(elements);
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        block.draw(g, x1, y1, x2, y2);
    }

    @Override
    public int minHeight() {
        return block.minHeight();
    }

    @Override
    public int computeHeight(Graphics2D g, int width) {
        return block.computeHeight(g, width);
    }
}
