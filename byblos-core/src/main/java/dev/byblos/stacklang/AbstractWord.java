package dev.byblos.stacklang;

import static java.util.Objects.requireNonNull;

public abstract class AbstractWord implements Word {
    private final String name;
    private final String signature;

    protected AbstractWord(String name, String signature) {
        this.name = requireNonNull(name);
        this.signature = requireNonNull(signature);
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final String signature() {
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractWord that = (AbstractWord) o;
        return com.google.common.base.Objects.equal(name, that.name)
                && com.google.common.base.Objects.equal(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(name, signature);
    }
}
