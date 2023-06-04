package dev.byblos.chart.util;

import java.awt.*;
import java.io.IOException;

public class Fonts {

    private static Font loadTrueTypeFont(String resource)  {
        try (var in = Fonts.class.getClassLoader().getResourceAsStream(resource)) {
            return Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(12.0f);
        } catch (IOException|FontFormatException e) {
            throw new RuntimeException(String.format("cannot load true type font '%s'", resource), e);
        }
    }

    /**
     * Load a font from the system or from the classpath.
     */
    public static Font loadFont(String font) {
        if (font.endsWith(".ttf")) {
            return loadTrueTypeFont(font);
        }
        return new Font(font, Font.PLAIN, 12);
    }

    /**
     * Font that is provided with the library and thus will be available on all systems. There
     * may be slight differences in the rendering on different versions of the JDK.
     */
    public static Font DEFAULT = loadFont("fonts/RobotoMono-Regular.ttf");

    /**
     * Returns true if the JDK and OS being used match those used to generate the blessed
     * reference images for test cases. On other systems there will be slight differences in
     * the font rendering causing diffs.
     */
    public static boolean shouldRunTests() {
        // May work on others, but 15 is the earliest confirmed to have consistent rendering
        // at this point
        var isAtLeastJdk15 = Double.parseDouble(System.getProperty("java.specification.version")) >= 15.0;
        var isMacOS = "Mac OS X".equals(System.getProperty("os.name"));
        var isArm = "aarch64".equals(System.getProperty("os.arch"));
        return isAtLeastJdk15 && isMacOS && !isArm;
    }

    private Fonts() {
        // Do not instantiate.
    }
}
