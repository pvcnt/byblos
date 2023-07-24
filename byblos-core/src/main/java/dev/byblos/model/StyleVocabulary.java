package dev.byblos.model;

import com.google.common.base.Joiner;
import dev.byblos.stacklang.*;
import dev.byblos.util.Strings;
import dev.byblos.stacklang.Macro;

import java.util.HashMap;
import java.util.List;

public final class StyleVocabulary implements Vocabulary {
    public final static Vocabulary INSTANCE = new StyleVocabulary();

    private StyleVocabulary() {
        // Singleton.
    }

    @Override
    public String name() {
        return "style";
    }

    @Override
    public List<Vocabulary> dependsOn() {
        return List.of(DataVocabulary.INSTANCE);
    }

    @Override
    public List<Word> words() {
        return List.of(
                new Alpha(),
                new Color(),
                new Palette(),
                new Decode(),
                new SearchAndReplace(),
                new StyleWord("axis"),
                new StyleWord("legend"),
                new StyleWord("limit"),
                new Macro("head", List.of(":limit")),
                new StyleWord("lw"),
                new StyleWord("ls"),
                new StyleWord("order"),
                new StyleWord("sort"),
                new Macro("area", List.of("area", ":ls")),
                new Macro("line", List.of("line", ":ls")),
                new Macro("stack", List.of("stack", ":ls")),
                new Macro("vspan", List.of("vspan", ":ls"))
        );
    }

    private static class StyleWord extends SimpleWord {
        protected StyleWord(String name) {
            super(name, "TimeSeriesExpr String -- StyleExpr");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isString, TypeUtils::isPresentationType);
        }

        @Override
        protected final Stack execute(Stack stack) {
            var v = (String) stack.get(0);
            var t = TypeUtils.asPresentationType(stack.get(1));
            var settings = new HashMap<>(t.settings());
            settings.put(name(), v);
            var expr = new StyleExpr(t.expr(), settings);
            return stack.popAndPush(2, expr);
        }
    }

    static final class Alpha extends SimpleWord {
        Alpha() {
            super("alpha", "TimeSeriesExpr String -- StyleExpr");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isString, TypeUtils::isPresentationType);
        }

        @Override
        protected Stack execute(Stack stack) {
            var v = (String) stack.get(0);
            var t = TypeUtils.asPresentationType(stack.get(1));
            var settings = new HashMap<>(t.settings());
            var color = t.settings().get("color");
            if (null == color) {
                settings.put("alpha", v);
            } else {
                settings.put("color", withAlpha(color, v));
                settings.remove("alpha");
            }
            var expr = new StyleExpr(t.expr(), settings);
            return stack.popAndPush(2, expr);
        }

        private String withAlpha(String color, String alpha) {
            var a = Integer.parseInt(alpha, 16);
            var c = Strings.parseColor(color);
            var nc = new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), a);
            return String.format("%08x", nc.getRGB());
        }
    }

    static final class Color extends SimpleWord {
        Color() {
            super("color", "TimeSeriesExpr String -- StyleExpr");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isString, TypeUtils::isPresentationType);
        }

        @Override
        protected Stack execute(Stack stack) {
            var v = (String) stack.get(0);
            var t = TypeUtils.asPresentationType(stack.get(1));
            var settings = new HashMap<>(t.settings());
            settings.put("color", v);
            settings.remove("alpha");
            settings.remove("palette");
            var expr = new StyleExpr(t.expr(), settings);
            return stack.popAndPush(2, expr);
        }
    }

    static final class Palette extends SimpleWord {
        Palette() {
            super("palette", "TimeSeriesExpr String -- StyleExpr");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(v -> (TypeUtils.isString(v) || TypeUtils.isStringList(v)), TypeUtils::isPresentationType);
        }

        @Override
        protected Stack execute(Stack stack) {
            var v = TypeUtils.isStringList(stack.get(0))
                    ? "(," + Joiner.on(",").join(TypeUtils.asStringList(stack.get(0))) + ",)"
                    : (String) stack.get(0);
            var t = TypeUtils.asPresentationType(stack.get(1));
            var settings = new HashMap<>(t.settings());
            settings.put("palette", v);
            settings.remove("color");
            settings.remove("alpha");
            var expr = new StyleExpr(t.expr(), settings);
            return stack.popAndPush(2, expr);
        }
    }

    static final class Decode extends SimpleWord {
        Decode() {
            super("decode", "TimeSeriesExpr String -- StyleExpr");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isString, TypeUtils::isPresentationType);
        }

        @Override
        protected Stack execute(Stack stack) {
            var v = TypeUtils.asString(stack.get(0));
            var t = TypeUtils.asPresentationType(stack.get(1));
            var transform = String.format("%s,:%s", v, name());
            return stack.popAndPush(2, sed(t, transform));
        }
    }

    static final class SearchAndReplace extends SimpleWord {
        SearchAndReplace() {
            super("s", "TimeSeriesExpr s:String r:String -- StyleExpr");
        }

        @Override
        public boolean matches(Stack stack) {
            return stack.matches(TypeUtils::isString, TypeUtils::isString, TypeUtils::isPresentationType);
        }

        @Override
        protected Stack execute(Stack stack) {
            var r = TypeUtils.asString(stack.get(0));
            var s = TypeUtils.asString(stack.get(1));
            var t = TypeUtils.asPresentationType(stack.get(2));
            var transform = String.format("%s,%s,:%s", s, r, name());
            return stack.popAndPush(3, sed(t, transform));
        }
    }

    private static StyleExpr sed(StyleExpr expr, String transform) {
        var v = expr.settings().get("sed");
        var newTransform = (null == v) ? transform : String.format("%s,%s", v, transform);
        var settings = new HashMap<>(expr.settings());
        settings.put("sed", newTransform);
        return new StyleExpr(expr.expr(), settings);
    }
}