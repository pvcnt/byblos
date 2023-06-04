package dev.byblos.chart.graphics;

import java.awt.Graphics2D;

/**
 * Indicates that the height of the element is variable depending on the width. The most common
 * example is text that can wrap to fit the width available.
 */
public interface VariableHeight {
    int minHeight();

    int computeHeight(Graphics2D g, int width);
}
