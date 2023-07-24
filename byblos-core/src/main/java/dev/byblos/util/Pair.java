package dev.byblos.util;

import static java.util.Objects.requireNonNull;

public record Pair<T, U>(T first, U second) {
    public Pair {
        requireNonNull(first);
        requireNonNull(second);
    }
}
