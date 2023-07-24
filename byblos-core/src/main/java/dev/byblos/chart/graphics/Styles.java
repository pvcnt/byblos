package dev.byblos.chart.graphics;

import com.typesafe.config.Config;
import dev.byblos.util.Strings;

import java.awt.BasicStroke;
import java.awt.Stroke;

/**
 * Set of styles for rendering an element on a chart.
 */
public final class Styles {
    private final Style background;
    private final Style line;
    private final Style text;

    private static final Stroke solidStroke = new BasicStroke(1.0f);

    private static final Stroke dashedStroke = new BasicStroke(
            1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            1.0f,
            new float[]{1.0f, 1.0f},
            0.0f
    );

    /**
     * Constructor.
     *
     * @param background Style used for the background fill.
     * @param line       Style used to render lines.
     * @param text       Style used to render text.
     */
    public Styles(Style background, Style line, Style text) {
        this.background = background;
        this.line = line;
        this.text = text;
    }

    public static Styles fromConfig(Config config) {
        return new Styles(
                loadStyle(config, "background"),
                loadStyle(config, "line"),
                loadStyle(config, "text")
        );
    }

    private static Style loadStyle(Config config, String name) {
        var color = Strings.parseColor(config.getString(name + "-color"));
        var stroke = parseStroke(config.getString(name + "-stroke"));
        return new Style(color, stroke);
    }

    private static Stroke parseStroke(String s) {
        switch (s) {
            case "dashed":
                return dashedStroke;
            case "solid":
                return solidStroke;
            default:
                throw new IllegalArgumentException("unknown stroke: " + s);
        }
    }

    public Style background() {
        return background;
    }

    public Style line() {
        return line;
    }

    public Style text() {
        return text;
    }
}