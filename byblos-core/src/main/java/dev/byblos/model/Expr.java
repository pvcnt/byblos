package dev.byblos.model;

public interface Expr {
    /**
     * Returns a string that can be executed with the stack interpreter to create this expression.
     */
    default String exprString() {
        return toString();
    }
}
