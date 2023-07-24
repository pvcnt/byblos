package dev.byblos.stacklang;

import dev.byblos.util.Features;

import java.util.Map;

/**
 * State related to the execution of a stack language expression.
 *
 * @param interpreter      the interpreter that is performing the execution.
 * @param stack            the stack that maintains the state for the program.
 * @param variables        variables that can be set to keep state outside of the main stack. See the
 *                         `:get` and `:set` operators for more information.
 * @param initialVariables an initial set of variables used when beginning the execution. These values
 *                         will be used when operations like `:freeze` need to reset the interpreter to
 *                         the initial state.
 * @param frozenStack      a separate stack that has been frozen to prevent further modification. See the
 *                         `:freeze` operator for more information.
 * @param features         the set of features that are permitted for the execution.
 */
public record Context(
        Interpreter interpreter,
        Stack stack,
        Map<String, Object> variables,
        Map<String, Object> initialVariables,
        Stack frozenStack,
        Features features
) {
    public static Context initial(Interpreter interpreter) {
        return new Context(interpreter, Stack.of(), Map.of(), Map.of(), Stack.of(), Features.STABLE);
    }

    public static Context initial(Interpreter interpreter, Map<String, Object> variables, Features features) {
        return new Context(interpreter, Stack.of(), variables, variables, Stack.of(), features);
    }

    public Context withStack(Stack stack) {
        return new Context(interpreter, stack, variables, initialVariables, frozenStack, features);
    }

    public Context withVariables(Map<String, Object> variables) {
        return new Context(interpreter, stack, variables, initialVariables, frozenStack, features);
    }

    /**
     * Remove the contents of the stack and push them onto the frozen stack. The variable
     * state will also be cleared.
     */
    public Context freeze() {
        return new Context(interpreter, Stack.of(), initialVariables, Map.of(), stack.concat(frozenStack), features);
    }

    /**
     * Combine the stack and frozen stack to a final result stack. The frozen contents will
     * be older entries on the final result stack.
     */
    public Context unfreeze() {
        return new Context(interpreter, stack.concat(frozenStack), variables, initialVariables, Stack.of(), features);
    }
}