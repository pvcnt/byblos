package dev.byblos.chart.graphics;

import dev.byblos.chart.model.Scale;

/**
 * Helper functions for creating the scales to map data values and times to the
 * pixel location for the image.
 */
public final class Scales {

    /**
     * Maps a double value to a pixel location. Typically used for the value scales.
     */
    interface DoubleScale {
        int apply(double v);
    }

    /**
     * Maps a long value to a pixel location. Typically used for time scales.
     */
    interface LongScale {
        int apply(long v);
    }

    /**
     * Factory for creating a value scale based on the min and max values for the input
     * data and the min and max pixel location.
     */
    interface DoubleFactory {
        DoubleScale apply(double d1, double d2, int r1, int r2);
    }


    /**
     * Returns the appropriate Y-value scale factory for the scale enum type.
     */
    public static DoubleFactory factory(Scale s) {
        switch (s) {
            case LINEAR:
                return yscale(linear);
            case LOGARITHMIC:
                return yscale(logarithmic);
            case POWER_2:
                return yscale(power(2.0));
            case SQRT:
                return yscale(power(0.5));
            default:
                throw new AssertionError();
        }
    }

    /**
     * Factory for a linear mapping.
     */
    public static DoubleFactory linear = (double d1, double d2, int r1, int r2) -> {
        var pixelSpan = (d2 - d1) / (r2 - r1);
        return v -> (int) ((v - d1) / pixelSpan) + r1;
    };

    private static double log10(double value) {
        if (value > 0.0) {
            return Math.log10(value + 1.0);
        }
        if (value < 0.0) {
            return -Math.log10(-(value - 1.0));
        }
        return 0;
    }

    /**
     * Factory for a logarithmic mapping. This is using logarithm for the purposes of
     * visualization, so `vizlog(0) == 0` and for `v < 0`, `vizlog(v) == -log(-v)`.
     */
    public static DoubleFactory logarithmic = (double d1, double d2, int r1, int r2) -> {
        var lg1 = log10(d1);
        var lg2 = log10(d2);
        var scale = linear.apply(lg1, lg2, r1, r2);
        return v -> scale.apply(log10(v));
    };

    private static double pow(double value, double exp) {
        if (value > 0.0) {
            return Math.pow(value, exp);
        }
        if (value < 0.0) {
            return -Math.pow(-value, exp);
        }
        return 0;
    }

    /**
     * Factory for a power mapping.
     */
    public static DoubleFactory power(double exp) {
        return (d1, d2, r1, r2) -> {
            var p1 = pow(d1, exp);
            var p2 = pow(d2, exp);
            var scale = linear.apply(p1, p2, r1, r2);
            return v -> scale.apply(pow(v, exp));
        };
    }

    /**
     * Converts a value scale to what is needed for the Y-Axis. Takes into account that
     * the pixel coordinates increase in the opposite direction from the view needed for
     * showing to the user.
     */
    public static DoubleFactory yscale(DoubleFactory s) {
        return (d1, d2, r1, r2) -> {
            var std = s.apply(d1, d2, r1, r2);
            return v -> r2 - std.apply(v) + r1;
        };
    }

    /**
     * Factory for creating a mapping for time values.
     */
    public static LongScale time(long t1, long t2, long step, int r1, int r2) {
        var d1 = (double) t1;
        var d2 = (double) t2;
        var dr = (d2 - d1) / step;
        var pixelsPerStep = (r2 - r1) / dr;
        return v -> (int) ((v - d1) / step * pixelsPerStep) + r1;
    }

    private Scales() {
        // Do not instantiate.
    }
}
