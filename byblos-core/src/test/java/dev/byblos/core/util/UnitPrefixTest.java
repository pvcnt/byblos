package dev.byblos.core.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UnitPrefix}.
 */
public class UnitPrefixTest {

    @Test
    void decimalWhenIsNearlyZero() {
        assertThat(UnitPrefix.decimal(1e-13).text()).isEmpty();
    }

    @Test
    void decimalWhenIsInfinity() {
        assertThat(UnitPrefix.decimal(Double.POSITIVE_INFINITY).text()).isEmpty();
    }

    @Test
    void decimalWhenIsNaN() {
        assertThat(UnitPrefix.decimal(Double.NaN).text()).isEmpty();
    }

    @Test
    void decimalMilli() {
        assertThat(UnitPrefix.decimal(1.23e-3).text()).isEqualTo("milli");
        assertThat(UnitPrefix.decimal(-1.23e-3).text()).isEqualTo("milli");
    }

    @Test
    void decimalKilo() {
        assertThat(UnitPrefix.decimal(1.23e3).text()).isEqualTo("kilo");
        assertThat(UnitPrefix.decimal(-1.23e3).text()).isEqualTo("kilo");
    }

    @Test
    void decimalMega() {
        assertThat(UnitPrefix.decimal(1.23e6).text()).isEqualTo("mega");
        assertThat(UnitPrefix.decimal(-1.23e6).text()).isEqualTo("mega");
    }

    @Test
    void decimalGiga() {
        assertThat(UnitPrefix.decimal(1.23e9).text()).isEqualTo("giga");
        assertThat(UnitPrefix.decimal(-1.23e9).text()).isEqualTo("giga");
    }

    @Test
    void binaryWhenIsNearlyZero() {
        assertThat(UnitPrefix.binary(1e-13).text()).isEmpty();
    }

    @Test
    void binaryWhenIsInfinity() {
        assertThat(UnitPrefix.binary(Double.POSITIVE_INFINITY).text()).isEmpty();
    }

    @Test
    void binaryWhenIsNaN() {
        assertThat(UnitPrefix.binary(Double.NaN).text()).isEmpty();
    }

    @Test
    void binaryMilli() {
        assertThat(UnitPrefix.binary(1.23e-3).text()).isEqualTo("milli");
        assertThat(UnitPrefix.binary(-1.23e-3).text()).isEqualTo("milli");
    }

    @Test
    void binaryKibi() {
        assertThat(UnitPrefix.binary(1023.0).text()).isEmpty();
        assertThat(UnitPrefix.binary(1.23e3).text()).isEqualTo("kibi");
        assertThat(UnitPrefix.binary(-1.23e3).text()).isEqualTo("kibi");
    }

    @Test
    void binaryMebi() {
        assertThat(UnitPrefix.binary(1.23e6).text()).isEqualTo("mebi");
        assertThat(UnitPrefix.binary(-1.23e6).text()).isEqualTo("mebi");
    }

    @Test
    void binaryGibi() {
        assertThat(UnitPrefix.binary(1.23e9).text()).isEqualTo("gibi");
        assertThat(UnitPrefix.binary(-1.23e9).text()).isEqualTo("gibi");
    }

    @Test
    void formatWhenIsMaxValue() {
        var s = UnitPrefix.decimal(Double.MAX_VALUE).format(Double.MAX_VALUE);
        assertThat(s).isEqualTo(" 2e+308");
    }

    @Test
    void formatWhenIsMinValue() {
        var s = UnitPrefix.decimal(-Double.MAX_VALUE).format(-Double.MAX_VALUE);
        assertThat(s).isEqualTo("-2e+308");
    }
}
