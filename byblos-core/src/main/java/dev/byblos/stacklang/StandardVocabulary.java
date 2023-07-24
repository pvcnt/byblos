package dev.byblos.stacklang;

import dev.byblos.model.TypeUtils;

import java.util.HashMap;
import java.util.List;

import static dev.byblos.model.TypeUtils.asString;

public final class StandardVocabulary implements Vocabulary {
    public final static Vocabulary INSTANCE = new StandardVocabulary();

    private StandardVocabulary() {
        // Singleton.
    }

    @Override
    public String name() {
        return "std";
    }

    @Override
    public List<Vocabulary> dependsOn() {
        return List.of();
    }

    @Override
    public List<Word> words() {
        return List.of(
                new Clear(),
                new Freeze(),
                new Dup(),
                new Drop(),
                new NDrop(),
                new Get(),
                new Set(),
                new Rot(),
                new ReverseRot(),
                new Swap(),
                new Depth(),
                new Over(),
                new Pick(),
                new Macro("2over", List.of("over", "over"))
        );
    }

    /**
     * Remove all items from the stack.
     */
    static final class Clear extends SimpleWord {
        Clear() {
            super("clear", "* -- <empty>");
        }

        @Override
        public boolean matches(Stack stack) {
            return true;
        }

        @Override
        protected Stack execute(Stack stack) {
            return Stack.of();
        }
    }

    /**
     * Freeze the current contents of the stack so that it cannot be modified.
     */
    static final class Freeze extends AbstractWord {
        Freeze() {
            super("freeze", "* -- <empty>");
        }

        @Override
        public boolean matches(Stack stack) {
            return true;
        }

        @Override
        public Context execute(Context context) {
            return context.freeze();
        }
    }

    /**
     * Duplicate the item on the top of the stack.
     */
    static final class Dup extends SimpleWord {
        Dup() {
            super("dup", "a -- a a");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.nonEmpty();
        }

        @Override
        protected Stack execute(Stack stack) {
            return stack.push(stack.get(0));
        }
    }

    /**
     * Remove the item on the top of the stack.
     */
    static final class Drop extends SimpleWord {
        Drop() {
            super("drop", "a -- ");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.nonEmpty();
        }

        @Override
        protected Stack execute(Stack stack) {
            return stack.drop(1);
        }
    }

    /**
     * Drop the top N items from the stack.
     */
    static final class NDrop extends SimpleWord {
        NDrop() {
            super("ndrop", "aN ... a0 N -- aN");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isInt);
        }

        @Override
        protected Stack execute(Stack stack) {
            var n = TypeUtils.asInt(stack.get(0));
            return stack.drop(n + 1);
        }
    }

    /**
     * Set the value of a variable.
     */
    static final class Set extends AbstractWord {
        Set() {
            super("set", "k v -- ");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(v -> true, TypeUtils::isString);
        }

        @Override
        public Context execute(Context context) {
            var value = context.stack().get(0);
            var key = asString(context.stack().get(1));
            var variables = new HashMap<>(context.variables());
            variables.put(key, value);
            return context.withStack(context.stack().drop(2)).withVariables(variables);
        }
    }

    /**
     * Get the value of a variable and push it on the stack.
     */
    static final class Get extends AbstractWord {
        Get() {
            super("get", "k -- vars[k]");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isString);
        }

        @Override
        public Context execute(Context context) throws InvalidSyntaxException {
            var key = asString(context.stack().get(0));
            var value = context.variables().get(key);
            if (null == value) {
                throw new InvalidSyntaxException(String.format("unknown variable '%s'", key));
            }
            return context.withStack(context.stack().popAndPush(value));
        }
    }

    /**
     * Rotate the stack so that the item at the bottom is now at the top.
     */
    static final class Rot extends SimpleWord {
        Rot() {
            super("rot", "a ... b -- ... b a");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.nonEmpty();
        }


        @Override
        protected Stack execute(Stack stack) {
            return stack.dropRight(1).push(stack.get(stack.size() - 1));
        }
    }

    /**
     * Rotate the stack so that the item at the top is now at the bottom.
     */
    static final class ReverseRot extends SimpleWord {
        ReverseRot() {
            super("-rot", "* a b -- b * a");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.nonEmpty();
        }


        @Override
        protected Stack execute(Stack stack) {
            return stack.drop(1).pushRight(stack.get(0));
        }
    }

    /**
     * Swap the top two items on the stack.
     */
    static final class Swap extends SimpleWord {
        Swap() {
            super("swap", "a b -- b a");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.size() >= 2;
        }


        @Override
        protected Stack execute(Stack stack) {
            return Stack.of(stack.get(1), stack.get(0)).concat(stack.drop(2));
        }
    }

    /**
     * Compute the depth of the stack.
     */
    static final class Depth extends SimpleWord {
        Depth() {
            super("depth", " -- N");
        }

        @Override
        public boolean matches(Stack stack) {
            return true;
        }


        @Override
        protected Stack execute(Stack stack) {
            // The depth is pushed as a string because we don't currently have a way to indicate the
            // type. The IntType extractor will match the string for operations that need to extract
            // and int.
            return stack.push(String.valueOf(stack.size()));
        }
    }

    /**
     * Copy the item in the second position on the stack to the top.
     */
    static final class Over extends SimpleWord {
        Over() {
            super("over", "a b -- a b a");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.size() >= 2;
        }


        @Override
        protected Stack execute(Stack stack) {
            return stack.push(stack.get(1));
        }
    }

    /**
     * Pick an item in the stack and put a copy on the top.
     */
    static final class Pick extends SimpleWord {
        Pick() {
            super("pick", "aN ... a0 N -- aN ... a0 aN");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isInt);
        }


        @Override
        protected Stack execute(Stack stack) throws InvalidSyntaxException {
            var n = TypeUtils.asInt(stack.get(0));
            if (n > stack.size() - 2) {
                throw new InvalidSyntaxException("cannot pick item " + n + " from stack of size " + (stack.size() - 1));
            }
            return stack.popAndPush(stack.get(n + 1));
        }
    }
}
