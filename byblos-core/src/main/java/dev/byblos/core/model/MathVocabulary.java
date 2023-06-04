package dev.byblos.core.model;

import dev.byblos.core.stacklang.SimpleWord;
import dev.byblos.core.stacklang.Stack;
import dev.byblos.core.stacklang.Vocabulary;
import dev.byblos.core.stacklang.Word;

import java.util.List;

import static dev.byblos.core.model.TypeUtils.*;

public final class MathVocabulary implements Vocabulary {

    public final static Vocabulary INSTANCE = new MathVocabulary();

    private MathVocabulary() {
        // Singleton.
    }

    @Override
    public String name() {
        return "math";
    }

    @Override
    public List<Vocabulary> dependsOn() {
        return List.of(DataVocabulary.INSTANCE);
    }

    @Override
    public List<Word> words() {
        return List.of(
                new Const(),
                new Random(),
                new SeededRandom(),
                new Time()
        );
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

    static final class Random extends SimpleWord {

        Random() {
            super("random", " -- TimeSeriesExpr");
        }

        @Override
        public boolean matches(Stack stack) {
            return true;
        }

        @Override
        protected Stack execute(Stack stack) {
            return stack.push(new RandomExpr());
        }
    }

    static final class SeededRandom extends SimpleWord {

        SeededRandom() {
            super("srandom", "seed:Integer -- TimeSeriesExpr");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isInt);
        }

        @Override
        protected Stack execute(Stack stack) {
            var expr = new SeededRandomExpr(asInt(stack.get(0)));
            return stack.popAndPush(expr);
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
