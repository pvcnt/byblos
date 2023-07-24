package dev.byblos.model;

import dev.byblos.stacklang.Interpreter;
import dev.byblos.stacklang.InvalidSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static dev.byblos.model.TypeUtils.asPresentationType;
import static dev.byblos.model.TypeUtils.isPresentationType;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StyleExpr}.
 */
public class StyleExprTest {

    private final Interpreter interpreter = new Interpreter(StyleVocabulary.INSTANCE.allWords());

    @ParameterizedTest
    @CsvSource(value = {
            "hex,:decode | one!%&?",
            "^([a-z]+).*$,prefix [$1],:s | prefix [one]",
            "^(?<prefix>[a-z]+).*$,prefix [$prefix],:s | prefix [one]",
            "_.*,,:s | one",
            "(_[A-F0-9]{2}), $1,:s,hex,:decode | one ! % & ?",
            "hex,:decode,(_[A-F0-9]{2}), $1,:s | one!%&?",
            "hex,:decode,%,_25,:s | one!_25&?",
            "hex,:decode,%,_25,:s,hex,:decode | one!%&?",
            "none,:decode,%,_25,:s,hex,:decode | one!%&?"
    }, delimiter = '|')
    void legend(String sed, String expected) {
        var expr = newExpr("$b", sed);
        var ts = newTimeSeries("foo", Map.of("a", "1", "b", "one_21_25_26_3F"));
        assertThat(expr.legend(ts)).isEqualTo(expected);
    }

    @Test
    void alphaAndColorArePreservedWithExprString() throws Exception {
        var expr = eval("1,ff0000,:color,40,:alpha");
        var result = eval(expr.toString());
        assertThat(expr.settings()).isEqualTo(result.settings());
    }

    @Test
    void alphaAndPaletteArePreservedWithExprString() throws Exception {
        var expr = eval("1,reds,:palette,40,:alpha");
        var result = eval(expr.toString());
        assertThat(expr.settings()).isEqualTo(result.settings());
    }

    private static StyleExpr newExpr(String legend, String sed) {
        return new StyleExpr(new DataExpr("up"), Map.of("legend", legend, "sed", sed));
    }

    private static TimeSeries newTimeSeries(String label, Map<String, String> tags) {
        var data = new FunctionTimeSeq(1, t -> Double.NaN);
        return new TimeSeries(data, label, tags);
    }

    private StyleExpr eval(String str) throws InvalidSyntaxException {
        var stack = interpreter.execute(str).stack();
        if (stack.nonEmpty() && isPresentationType(stack.get(0))) {
            return asPresentationType(stack.get(0));
        }
        throw new AssertionError("not a presentation type: " + stack);
    }
}
