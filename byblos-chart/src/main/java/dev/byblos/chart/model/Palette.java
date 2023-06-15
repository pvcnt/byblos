package dev.byblos.chart.model;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import dev.byblos.chart.Colors;
import dev.byblos.core.util.Strings;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public final class Palette {

    private static final Map<String, Palette> RESOURCES = new ConcurrentHashMap<>();

    public static final Palette DEFAULT = new Palette(
            "default",
            List.of(
                    Color.RED,
                    Color.GREEN,
                    Color.BLUE,
                    Color.MAGENTA,
                    Color.YELLOW,
                    Color.CYAN,
                    Color.PINK,
                    Color.ORANGE
            )
    );

    private final String name;
    private final List<Color> colors;

    private Palette(String name, List<Color> colors) {
        checkArgument(!colors.isEmpty(), "palette must contain at least one color");
        this.name = requireNonNull(name);
        this.colors = requireNonNull(colors);
    }


    public static Palette singleColor(Color c) {
        return new Palette(String.format("%08X", c.getRGB()), List.of(c));
    }

    /**
     * Create a palette from a file in the classpath named `palettes/{name}_palette.txt`. The
     * file should have one color per line in a format supported by `Strings.parseColor`.
     */
    public static Palette fromResource(String name) {
        return RESOURCES.computeIfAbsent(name, Palette::loadFromResource);
    }

    private static Palette loadFromResource(String name) {
        try {
            var colors = Colors.load(String.format("palettes/%s_palette.txt", name));
            return new Palette(name, colors);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("invalid palette name: '%s'", name));
        }
    }

    /**
     * Creates a palette instance from a description string. The description can be an explicit
     * list of colors or the name of a palette file in the classpath. An explicit list is specified
     * as an ASL list of colors. For example:
     * <p>
     * ```
     * (,f00,00ff00,000000ff,)
     * ```
     * <p>
     * The color values will be parsed using `Strings.parseColor`.
     * Otherwise the description will be used to find a palette file in the classpath named
     * `palettes/{desc}_palette.txt` that has one color per line.
     */
    public static Palette fromString(String desc) {
        // `colors:` prefix is deprecated, use list variant that is consistent between
        // the url parameter and expression
        if (desc.startsWith("colors:")) {
            return new Palette("colors", parseColors(desc.substring("colors:".length())));
        }
        if (desc.startsWith("(")) {
            return new Palette("colors", parseColors(desc));
        }
        return fromResource(desc);
    }

    private static List<Color> parseColors(String colorsString) {
        return Splitter.on(",").trimResults().omitEmptyStrings().splitToStream(colorsString)
                .filter(s -> !"(".equals(s) && !")".equals(s))
                .map(Strings::parseColor)
                .collect(Collectors.toList());
    }

    public Palette withAlpha(int alpha) {
        return new Palette(name, colors.stream().map(c -> Colors.withAlpha(c, alpha)).collect(Collectors.toList()));
    }

    public Iterator<Color> iterator() {
        return new Iterator<>() {
            private int pos = -1;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Color next() {
                pos += 1;
                if (pos >= colors.size()) {
                    pos = 0;
                }
                return colors.get(pos);
            }
        };
    }


    /**
     * Rotates through the colors in the palette based on the index, returning a
     * deterministic color.
     *
     * @param i A positive integer value.
     * @return A deterministic color in the palette.
     */
    public Color colors(int i) {
        var index = Math.abs(i) % colors.size();
        return colors.get(index);
    }

    public int size() {
        return colors.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Palette palette = (Palette) o;
        return Objects.equal(name, palette.name) && Objects.equal(colors, palette.colors);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, colors);
    }

}
