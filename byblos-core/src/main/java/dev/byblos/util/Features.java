package dev.byblos.util;

import java.util.Locale;

/**
 * Set of features that are enabled for the API.
 */
public enum Features {
    /** Default feature set that is stable and the user can rely on. */
    STABLE,

    /**
     * Indicates that unstable features should be enabled for testing by early adopters.
     * Features in this set can change at anytime without notice. A feature should not stay
     * in this state for a long time. A few months should be considered an upper bound.
     */
    UNSTABLE;

    public static Features fromString(String s) {
        return valueOf(s.toUpperCase(Locale.US));
    }
}