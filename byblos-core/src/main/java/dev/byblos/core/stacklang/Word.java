package dev.byblos.core.stacklang;

import java.util.List;

public interface Word {
    /**
     * Return the name used to refer to this command.
     */
    String name();

    /**
     * Return the signature of the method showing the before and after effect on the stack.
     */
    String signature();

    /**
     * Returns whether this operation is considered stable. New operations should override
     * this method to return false until the API is finalized.
     */
    default boolean isStable() {
        return true;
    }

    /**
     * Check whether this word can be executed against the current stack. Can be used as a basis for
     * finding auto-completion candidates.
     */
    boolean matches(Stack stack);

    /**
     * Execute this command against the provided context.
     */
    Context execute(Context context) throws InvalidSyntaxException;
}
