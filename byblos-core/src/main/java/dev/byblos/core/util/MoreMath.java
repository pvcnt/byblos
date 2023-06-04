package dev.byblos.core.util;

public final class MoreMath {
    /**
     * Check if a double value is nearly zero, i.e., within a small amount from 0. For our purposes
     * the small amount is `1e-12`. Not sure of the original reason for choosing that amount, but it
     * has been used for a long time.
     */
    public static boolean isNearlyZero(double v) {
        return Double.isNaN(v) || java.lang.Math.abs(v - 0.0) < 1e-12;
    }

    /**
     * Convert a double value to a boolean. NaN and nearly 0 values are considered false, all other
     * values are true.
     */
    public static boolean toBoolean(double v) {
        return !isNearlyZero(v);
    }

    private MoreMath() {
        // Do not instantiate.
    }
}
