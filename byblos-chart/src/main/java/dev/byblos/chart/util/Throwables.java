package dev.byblos.chart.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.byblos.core.stacklang.InvalidSyntaxException;

public final class Throwables {
    /**
     * Returns whether a given exception is a user error.
     *
     * A user error means that it was caused by a bad request from the user,
     * as opposed to a system error that is caused by a bug or the environment.
     *
     * @param t An exception.
     */
    public static boolean isUserError(Throwable t) {
        return (t instanceof IllegalArgumentException || t instanceof IllegalStateException || t instanceof JsonProcessingException || t instanceof InvalidSyntaxException);
    }

    /**
     * Returns an error message safe to be displayed to the user.
     *
     * In the case of a user error, we use the exception's message which is usually safe
     * to be shown to the user and allows them to fix their request. Otherwise, we use
     * a generic error message.
     *
     * @param t An exception.
     */
    public static String getHumanReadableErrorMessage(Throwable t) {
        return Throwables.isUserError(t) ? t.getMessage() : "Internal error";
    }

    private Throwables() {
        // Do not instantiate.
    }
}
