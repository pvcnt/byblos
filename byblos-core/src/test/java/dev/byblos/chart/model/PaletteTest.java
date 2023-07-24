package dev.byblos.chart.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Palette}.
 */
public class PaletteTest {
    @Test
    void fromStringWhenColorsIsIllegal() {
        assertThatThrownBy(() -> Palette.fromString("colors:foo"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromStringWhenColorsIsEmpty() {
        assertThatThrownBy(() -> Palette.fromString("colors:"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("palette must contain at least one color");
    }

    @Test
    void fromStringWhenColorsHasOneColor() {
        var p = Palette.fromString("colors:f00");
        assertThat(p.colors(0)).isEqualTo(Color.RED);
        assertThat(p.size()).isEqualTo(1);
    }

    @Test
    void fromStringWhenColorsHasTwoColors() {
        var p = Palette.fromString("colors:f00,00ff00");
        assertThat(p.colors(0)).isEqualTo(Color.RED);
        assertThat(p.colors(1)).isEqualTo(Color.GREEN);
        assertThat(p.size()).isEqualTo(2);
    }

    @Test
    void fromStringWhenColorsHasThreeColors() {
        var p = Palette.fromString("colors:f00,00ff00,ff0000ff");
        assertThat(p.colors(0)).isEqualTo(Color.RED);
        assertThat(p.colors(1)).isEqualTo(Color.GREEN);
        assertThat(p.colors(2)).isEqualTo(Color.BLUE);
        assertThat(p.size()).isEqualTo(3);
    }

    @Test
    void fromStringWhenParensIsIllegal() {
        assertThatThrownBy(() -> Palette.fromString("(,foo,)"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromStringWhenParensIsEmpty() {
        assertThatThrownBy(() -> Palette.fromString("(,)"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("palette must contain at least one color");;
    }

    @Test
    void fromStringWhenParensHasOneColor() {
        var p = Palette.fromString("(,f00,)");
        assertThat(p.colors(0)).isEqualTo(Color.RED);
        assertThat(p.size()).isEqualTo(1);
    }

    @Test
    void fromStringWhenParensHasTwoColors() {
        var p = Palette.fromString("(,f00,00ff00,)");
        assertThat(p.colors(0)).isEqualTo(Color.RED);
        assertThat(p.colors(1)).isEqualTo(Color.GREEN);
        assertThat(p.size()).isEqualTo(2);
    }

    @Test
    void fromStringWhenParensHasThreeColors() {
        var p = Palette.fromString("(,f00,00ff00,ff0000ff,)");
        assertThat(p.colors(0)).isEqualTo(Color.RED);
        assertThat(p.colors(1)).isEqualTo(Color.GREEN);
        assertThat(p.colors(2)).isEqualTo(Color.BLUE);
        assertThat(p.size()).isEqualTo(3);
    }

    @Test
    void fromStringWhenName() {
        var p = Palette.fromString("armytage");
        assertThat(p.colors(0)).isEqualTo(new Color(0, 117, 220));
        assertThat(p.size()).isEqualTo(24);
    }

    @Test
    void fromStringWhenNameIsIllegal() {
        assertThatThrownBy(() -> Palette.fromString("foo")).isInstanceOf(IllegalArgumentException.class);
    }
}
