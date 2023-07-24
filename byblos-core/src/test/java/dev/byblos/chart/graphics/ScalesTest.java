package dev.byblos.chart.graphics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Scales}.
 */
public class ScalesTest {
    @Test
    void linear() {
        var scale = Scales.linear.apply(0.0, 100.0, 0, 100);
        assertEquals(scale.apply(0.0), 0);
        assertEquals(scale.apply(10.0), 10);
        assertEquals(scale.apply(20.0), 20);
        assertEquals(scale.apply(30.0), 30);
        assertEquals(scale.apply(40.0), 40);
        assertEquals(scale.apply(50.0), 50);
        assertEquals(scale.apply(60.0), 60);
        assertEquals(scale.apply(70.0), 70);
        assertEquals(scale.apply(80.0), 80);
        assertEquals(scale.apply(90.0), 90);
        assertEquals(scale.apply(100.0), 100);
    }

    @Test
    void ylinear_l1_u2_h300() {
        var scale = Scales.yscale(Scales.linear).apply(1.0, 2.0, 0, 300);
        assertEquals(scale.apply(1.0), 300);
        assertEquals(scale.apply(2.0), 0);
    }

    @Test
    void logarithmic() {
        var scale = Scales.logarithmic.apply(0.0, 100.0, 0, 100);
        assertEquals(scale.apply(0.0), 0);
        assertEquals(scale.apply(10.0), 51);
        assertEquals(scale.apply(20.0), 65);
        assertEquals(scale.apply(30.0), 74);
        assertEquals(scale.apply(40.0), 80);
        assertEquals(scale.apply(50.0), 85);
        assertEquals(scale.apply(60.0), 89);
        assertEquals(scale.apply(70.0), 92);
        assertEquals(scale.apply(80.0), 95);
        assertEquals(scale.apply(90.0), 97);
        assertEquals(scale.apply(100.0), 100);
    }

    @Test
    void logarithmicNegative() {
        var scale = Scales.logarithmic.apply(-100.0, 0.0, 0, 100);
        assertEquals(scale.apply(0.0), 100);
        assertEquals(scale.apply(-10.0), 48);
        assertEquals(scale.apply(-20.0), 34);
        assertEquals(scale.apply(-30.0), 25);
        assertEquals(scale.apply(-40.0), 19);
        assertEquals(scale.apply(-50.0), 14);
        assertEquals(scale.apply(-60.0), 10);
        assertEquals(scale.apply(-70.0), 7);
        assertEquals(scale.apply(-80.0), 4);
        assertEquals(scale.apply(-90.0), 2);
        assertEquals(scale.apply(-100.0), 0);
    }

    @Test
    void logarithmicPositiveAndNegative() {
        var scale = Scales.logarithmic.apply(-100.0, 100.0, 0, 100);
        assertEquals(scale.apply(100.0), 100);
        assertEquals(scale.apply(50.0), 92);
        assertEquals(scale.apply(10.0), 75);
        assertEquals(scale.apply(0.0), 50);
        assertEquals(scale.apply(-10.0), 24);
        assertEquals(scale.apply(-50.0), 7);
        assertEquals(scale.apply(-100.0), 0);
    }

    @Test
    void logarithmicLessThanLowerBound() {
        var scale = Scales.logarithmic.apply(15.0, 100.0, 0, 100);
        assertEquals(scale.apply(0.0), -150);
        assertEquals(scale.apply(10.0), -20);
        assertEquals(scale.apply(20.0), 14);
        assertEquals(scale.apply(30.0), 35);
        assertEquals(scale.apply(40.0), 51);
        assertEquals(scale.apply(50.0), 62);
        assertEquals(scale.apply(60.0), 72);
        assertEquals(scale.apply(70.0), 80);
        assertEquals(scale.apply(80.0), 88);
        assertEquals(scale.apply(90.0), 94);
        assertEquals(scale.apply(100.0), 100);
    }
}
