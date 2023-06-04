package dev.byblos.chart.graphics;


import java.awt.Graphics2D;

/**
 * Draws a list item, i.e., a bullet with wrapped text.
 */
public final class ListItem implements Element, VariableHeight {
    private final Text text;
    private final Dimensions dims;
    private final int diameter;

    public ListItem(Text text) {
        this.text = text;
        this.dims = ChartSettings.dimensions(text.font());
        this.diameter = dims.width() - 2;
    }

    @Override
    public int minHeight() {
        return text.minHeight();
    }

    @Override
    public int computeHeight(Graphics2D g, int width) {
        return text.computeHeight(g, width - dims.width());
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        text.style().configure(g);
        var xoffset = (dims.width() - diameter) / 2;
        var yoffset = (dims.height() - diameter) / 2;
        g.fillOval(x1 + xoffset, y1 + yoffset, diameter, diameter);
        text.draw(g, x1 + dims.width(), y1, x2, y2);
    }
}
