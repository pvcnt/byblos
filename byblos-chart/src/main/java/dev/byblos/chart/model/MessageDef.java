package dev.byblos.chart.model;

import java.awt.*;

/**
 * Definition for a message that is included in the legend, but not displayed.
 */
public final class MessageDef implements DataDef {
    private final String label;
    private final Color color;

    /**
     * Constructor.
     *
     * @param label Label associated with the span to use in the legend.
     * @param color Color to use when rendering the text in the legend.
     */
    public MessageDef(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public MessageDef(String label) {
        this(label, Color.BLACK);
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public Color color() {
        return color;
    }

    @Override
    public MessageDef withColor(Color c) {
        return new MessageDef(label, c);
    }
}
