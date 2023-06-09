package dev.byblos.core.model;

import dev.byblos.core.stacklang.*;

import java.util.List;

import static dev.byblos.core.model.TypeUtils.asDouble;
import static dev.byblos.core.model.TypeUtils.asString;

public final class DataVocabulary implements Vocabulary {

    public final static Vocabulary INSTANCE = new DataVocabulary();

    private DataVocabulary() {
        // Singleton.
    }

    @Override
    public String name() {
        return "query";
    }

    @Override
    public List<Vocabulary> dependsOn() {
        return List.of(StandardVocabulary.INSTANCE);
    }

    @Override
    public List<Word> words() {
        return List.of(
                new Query(),
                new Const(),
                new Time()
        );
    }

    private static final class Query extends SimpleWord {
        private Query() {
            super("query", "String -- TimeSeriesExpr");
        }

        @Override
        protected Stack execute(Stack stack) {
            var expr = new DataExpr(asString(stack.get(0)));
            return stack.popAndPush(expr);
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isString);
        }
    }

    static final class Const extends SimpleWord {

        Const() {
            super("const", "Double -- TimeSeriesExpr");
        }

        @Override
        protected Stack execute(Stack stack) {
            var expr = new ConstantExpr(asDouble(stack.get(0)));
            return stack.popAndPush(expr);
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isDouble);
        }
    }

    static final class Time extends SimpleWord {

        Time() {
            super("time", "String -- TimeSeriesExpr");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isString);
        }

        @Override
        protected Stack execute(Stack stack) {
            var expr = new TimeExpr(asString(stack.get(0)));
            return stack.popAndPush(expr);
        }
    }
}
