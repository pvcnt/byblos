package dev.byblos.core.stacklang;

import dev.byblos.core.model.TypeUtils;

import java.util.HashMap;
import java.util.List;

import static dev.byblos.core.model.TypeUtils.asString;

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
                new Get(),
                new Set()
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
}
