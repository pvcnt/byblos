package dev.byblos.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MoreMath}.
 */
public class MoreMathTest {

    @Test
    void isNearlyZero() {
        assertThat(MoreMath.isNearlyZero(1.0)).isFalse();
        assertThat(MoreMath.isNearlyZero(-1000.0)).isFalse();
        assertThat(MoreMath.isNearlyZero(0.0)).isTrue();
        assertThat(MoreMath.isNearlyZero(1e-12)).isFalse();
        assertThat(MoreMath.isNearlyZero(1e-13)).isTrue();
        assertThat(MoreMath.isNearlyZero(Double.NaN)).isTrue();
    }
}
