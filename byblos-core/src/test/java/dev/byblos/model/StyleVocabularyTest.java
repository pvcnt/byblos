package dev.byblos.model;

import dev.byblos.stacklang.Interpreter;
import dev.byblos.stacklang.InvalidSyntaxException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.byblos.model.TypeUtils.asPresentationType;
import static dev.byblos.model.TypeUtils.isPresentationType;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StyleVocabulary}.
 */
public class StyleVocabularyTest {
    private final Interpreter interpreter = new Interpreter(StyleVocabulary.INSTANCE.allWords());

    @Test
    void noAdditionalStyle() throws Exception {
        var expr = eval("up");
        assertThat(expr).isEqualTo(new StyleExpr(new DataExpr("up"), Map.of()));
    }

    @Test
    void alphaWord() throws Exception {
        var expr = eval("up,40,:alpha");
        assertThat(expr).isEqualTo(new StyleExpr(new DataExpr("up"), Map.of("alpha", "40")));
    }

    @Test
    void colorWord() throws Exception {
        var expr = eval("up,f00,:color");
        assertThat(expr).isEqualTo(new StyleExpr(new DataExpr("up"), Map.of("color", "f00")));
    }

    @Test
    void alphaWordThenColorWord() throws Exception {
        var expr = eval("up,40,:alpha,f00,:color");
        assertThat(expr).isEqualTo(new StyleExpr(new DataExpr("up"), Map.of("color", "f00")));
    }

    @Test
    void alphaWordThenColorWordThenAlphaWord() throws Exception {
        var expr = eval("up,40,:alpha,f00,:color,60,:alpha");
        assertThat(expr).isEqualTo(new StyleExpr(new DataExpr("up"), Map.of("color", "60ff0000")));
    }

    private StyleExpr eval(String str) throws InvalidSyntaxException {
        var stack = interpreter.execute(str).stack();
        if (stack.nonEmpty() && isPresentationType(stack.get(0))) {
            return asPresentationType(stack.get(0));
        }
        throw new AssertionError("not a presentation type: " + stack);
    }
}
