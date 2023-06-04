package dev.byblos.chart.graphics;

import java.awt.Graphics2D;

/**
 * Reserves a fixed amount of vertical space.
 */
public final class HorizontalPadding implements Element, FixedHeight {
    private final int height;

    public HorizontalPadding(int height) {
        this.height = height;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        // Nothing to draw.
    }
}
