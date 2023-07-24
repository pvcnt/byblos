package dev.byblos.chart.graphics;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;

/**
 * Draw text with a single font and simple alignment.
 */
public final class Text implements Element, VariableHeight {

    private static final int rightPadding = 8;

    private final String str;
    private final Font font; // Default: ChartSettings.normalFont
    private final TextAlignment alignment;  // Default: TextAlignment.CENTER
    private final Style style; // Default: Style.default
    private final Dimensions dims;

    /**
     * Constructor.
     *
     * @param str       The text to display.
     * @param font      Font to use when rendering.
     * @param alignment Basic alignment setting to use when laying out the text in the provided space. Defaults to
     *                  center.
     * @param style     Style to use for rendering the text.
     */
    private Text(String str, Font font, TextAlignment alignment, Style style) {
        this.str = str;
        this.font = font;
        this.alignment = alignment;
        this.style = style;
        dims = ChartSettings.dimensions(font);
    }

    public static Text center(String str, Font font, Style style) {
        return new Text(str, font, TextAlignment.CENTER, style);
    }

    public static Text center(String str, Style style) {
        return center(str, ChartSettings.normalFont, style);
    }

    public static Text left(String str, Font font, Style style) {
        return new Text(str, font, TextAlignment.LEFT, style);
    }

    public static Text left(String str, Style style) {
        return left(str, ChartSettings.normalFont, style);
    }

    public static Text right(String str, Font font, Style style) {
        return new Text(str, font, TextAlignment.RIGHT, style);
    }

    public Font font() {
        return font;
    }

    public Style style() {
        return style;
    }

    public Text truncate(int width) {
        int maxChars = (width - Text.rightPadding) / dims.width();
        if (str.length() < maxChars) {
            return this;
        }
        if (maxChars < 5) {
            return new Text("", font, alignment, style);
        }
        return new Text(str.substring(0, maxChars - 5) + "...", font, alignment, style);
    }

    @Override
    public int minHeight() {
        return dims.height();
    }

    @Override
    public int computeHeight(Graphics2D g, int width) {
        var attrStr = new AttributedString(str);
        attrStr.addAttribute(TextAttribute.FONT, font);
        var iterator = attrStr.getIterator();
        var measurer = new LineBreakMeasurer(iterator, g.getFontRenderContext());

        var wrap = width - Text.rightPadding;
        var y = 0.0f;
        while (measurer.getPosition() < str.length()) {
            var layout = measurer.nextLayout((float) wrap);
            y += layout.getAscent() + layout.getDescent() + layout.getLeading();
        }
        return (int) Math.ceil(y);
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        style.configure(g);

        var attrStr = new AttributedString(str);
        attrStr.addAttribute(TextAttribute.FONT, font);
        var iterator = attrStr.getIterator();
        var measurer = new LineBreakMeasurer(iterator, g.getFontRenderContext());

        var width = x2 - x1;
        var wrap = width - Text.rightPadding;
        var y = (float) y1;
        while (measurer.getPosition() < str.length()) {
            var layout = measurer.nextLayout((float) wrap);
            y += layout.getAscent();
            switch (alignment) {
                case LEFT:
                    drawLeft(g, layout, x1, y);
                    break;
                case RIGHT:
                    drawRight(g, layout, x1, y, width);
                    break;
                case CENTER:
                    drawCenter(g, layout, x1, y, width);
                    break;
            }
            y += layout.getDescent() + layout.getLeading();
        }
    }

    private void drawLeft(Graphics2D g, TextLayout layout, int x1, float y) {
        layout.draw(g, x1 + 4.0f, y);
    }

    private void drawRight(Graphics2D g, TextLayout layout, int x1, float y, int width) {
        var rect = layout.getBounds();
        var x = x1 + width - 4.0f - (float) rect.getWidth();
        layout.draw(g, x, y);
    }

    private void drawCenter(Graphics2D g, TextLayout layout, int x1, float y, int width) {
        var rect = layout.getBounds();
        var x = x1 + (width - (float) rect.getWidth()) / 2;
        layout.draw(g, x, y);
    }
}
