package dev.byblos.chart;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Colors}.
 */
public class ColorsTest {
    @Test
    void loadEpicPalette() throws Exception {
        var actual = Colors.load("palettes/epic_palette.txt");
        assertThat(actual.get(0)).isEqualTo(Color.RED);
        assertThat(actual.get(1)).isEqualTo(Color.GREEN);
    }
}
