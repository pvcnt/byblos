package dev.byblos.chart.graphics;

import com.netflix.iep.config.ConfigManager;
import com.typesafe.config.Config;
import dev.byblos.chart.util.Fonts;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ChartSettings {

    private final static Config config = ConfigManager.load().getConfig("byblos.chart");

    /**
     * For some of the font operations a graphics context is needed. This is a simple dummy instance
     * that can be used for cases where we need to determine the size before the actual image object
     * is created.
     */
    private final static BufferedImage refImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    public final static Graphics2D refGraphics = refImage.createGraphics();

    /**
     * Base monospaced font used for graphics. Monospace is used to make the layout easier.
     */
    public static final Font monospaceFont = Fonts.loadFont(config.getString("fonts.monospace"));

    /**
     * Small sized monospaced font.
     */
    public static final Font smallFont = monospaceFont.deriveFont(10.0f);

    /**
     * Normal sized monospaced font.
     */
    public static final Font normalFont = monospaceFont;

    /**
     * Large sized monospaced font.
     */
    public static final Font largeFont = monospaceFont.deriveFont(14.0f);

    /**
     * Dimensions for a character using the small font.
     */
    public static final Dimensions smallFontDims = dimensions(smallFont);

    /**
     * Dimensions for a character using the normal font.
     */
    public static final Dimensions normalFontDims = dimensions(normalFont);

    /**
     * Dimensions for a character using the large font.
     */
    public static final Dimensions largeFontDims = dimensions(largeFont);

    /**
     * Minimum width required for text elements. Value was chosen to allow typical messages to
     * display with a reasonable level of wrapping.
     */
    public static final int minWidthForText = smallFontDims.width() * "Warnings: abcdef".length();

    /**
     * Minimum width required for text elements. Value was chosen to allow the typical legend with
     * stats to show cleanly. It also keeps the cutoff below the level of sizes that are frequently
     * used in practice.
     */
    public static final int minWidthForStats = smallFontDims.width() * 45;

    /**
     * Dashed stroke typically used for grid lines.
     */
    public static final Stroke dashedStroke = new BasicStroke(
            1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            1.0f,
            new float[]{1.0f, 1.0f},
            0.0f
    );


    /**
     * Determine the dimensions for a single character using `font`. It is assumed that the font
     * is monospaced.
     */
    public static Dimensions dimensions(Font font) {
        refGraphics.setFont(font);
        var m = refGraphics.getFontMetrics();
        return new Dimensions(m.stringWidth("X"), m.getHeight());
    }

    private static final Map<String, Theme> themes = new ConcurrentHashMap<>();

    public static Theme theme(String name) {
        if (!config.hasPath("theme." + name)) {
            throw new IllegalArgumentException(String.format("invalid theme name: '%s'", name));
        }
        return themes.computeIfAbsent(name, n -> Theme.fromConfig(config.getConfig("theme." + n)));
    }

    private ChartSettings() {
        // Do not instantiate.
    }
}
