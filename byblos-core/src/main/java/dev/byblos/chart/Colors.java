package dev.byblos.chart;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import dev.byblos.util.Strings;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

public final class Colors {

    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /**
     * Load a list of colors from a resource file.
     */
    public static List<Color> load(String name) throws IOException {
        return Resources.readLines(Resources.getResource(name), Charsets.UTF_8)
                .stream()
                .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                .map(Strings::parseColor)
                .toList();
    }


    private Colors() {

    }
}
