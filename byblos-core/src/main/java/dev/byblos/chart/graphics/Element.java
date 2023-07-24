package dev.byblos.chart.graphics;

import java.awt.Graphics2D;

/**
 * Base type for elements to draw as part of the chart.
 */
public interface Element {

    void draw(Graphics2D g, int x1, int y1, int x2, int y2);

    /**
     * Compute the width for the element if restricted to the specified height.
     */
    default int getWidth(Graphics2D g, int height) {
        if (this instanceof FixedWidth) {
            return ((FixedWidth) this).width();
        }
        if (this instanceof VariableWidth) {
            return ((VariableWidth) this).computeWidth(g, height);
        }
        return 0;
    }

    /**
     * Compute the height for the element if restricted to the specified width.
     */
    default int getHeight(Graphics2D g, int width) {
        if (this instanceof FixedHeight) {
            return ((FixedHeight) this).height();
        }
        if (this instanceof VariableHeight) {
            return ((VariableHeight) this).computeHeight(g, width);
        }
        return 0;
    }
}
