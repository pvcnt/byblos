package dev.byblos.chart.graphics;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Draws a set of elements vertically.
 */
public final class Block implements Element, VariableHeight {
    private final List<Element> elements;
    private final Color background;

    /**
     * Constructor.
     *
     * @param elements Set of elements to draw within the block.
     * @param background Fill color to use for the background of the block. If not
     *                   specified then the elements will be drawn directly over the existing content.
     */
    public Block(List<Element> elements, @Nullable Color background) {
        this.elements = elements;
        this.background = background;
    }

    public Block(List<Element> elements) {
        this(elements, null);
    }


    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        var width = x2 - x1;
        var height = y2 - y1;
        if (null != background) {
            g.setColor(background);
            g.fillRect(x1, y1, width, height);
        }

        var y = y1;
        for (var element: elements) {
            var h = element.getHeight(g, width);
            element.draw(g, x1, y, x1 + width, y + h);
            y += h;
        }
    }

    @Override
    public int minHeight() {
        return 0;
    }

    @Override
    public int computeHeight(Graphics2D g, int width) {
        return elements.stream().mapToInt(e -> e.getHeight(g, width)).sum();
    }
}
