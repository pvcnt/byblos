package dev.byblos.core.stacklang;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Interpreter}.
 */
public class InterpreterTest {
    private final Interpreter interpreter = new Interpreter(
            List.of(
                    new TestWord("push-foo", "* -- * foo", "foo"),
                    new Overloaded("overloaded", "one", true),
                    new Overloaded("overloaded", "two", true),
                    new Overloaded("overloaded", "three", true),
                    new Overloaded("overloaded2", "one", false),
                    new Overloaded("overloaded2", "two", true),
                    new Overloaded("overloaded2", "three", true),
                    new Overloaded("no-match", "one", false),
                    new Unstable()
            )
    );

    @Test
    void executeEmpty() throws Exception {
        var ctx = interpreter.execute(List.of());
        assertThat(ctx).isEqualTo(newContext(Stack.of()));
    }

    @Test
    void executeProgramAsList() throws Exception {
        var ctx = interpreter.execute(List.of("foo", "bar"));
        assertThat(ctx).isEqualTo(newContext(Stack.of("bar", "foo")));
    }

    @Test
    void executeProgramAsString() throws Exception {
        var ctx = interpreter.execute("foo,bar");
        assertThat(ctx).isEqualTo(newContext(Stack.of("bar", "foo")));
    }

    @Test
    void executeWord() throws Exception {
        var ctx = interpreter.execute(List.of(":push-foo"));
        assertThat(ctx).isEqualTo(newContext(Stack.of("foo")));
    }

    @Test
    void executeOverloadedWord() throws Exception {
        var ctx = interpreter.execute(List.of(":overloaded"));
        assertThat(ctx).isEqualTo(newContext(Stack.of("one")));
    }

    @Test
    void executeOverloadedWordAndSomeDontMatch() throws Exception {
        assertThat(interpreter.execute(List.of(":overloaded2")))
                .isEqualTo(newContext(Stack.of("two")));
    }

    @Test
    void executeWordWithNoMatches() {
        assertThatThrownBy(() -> interpreter.execute(List.of(":no-match")))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("no matches for word ':no-match' with stack [], candidates: [exception]");
    }

    @Test
    void executeUnstableWordFailsByDefault() {
        assertThatThrownBy(() -> interpreter.execute(List.of(":unstable")))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("to use :unstable enable unstable features");
    }

    @Test
    void executeUnknownWord() {
        assertThatThrownBy(() -> interpreter.execute(List.of("foo", ":unknown")))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("unknown word ':unknown'");
    }

    @Test
    void executeUnmatchedClosingParen() {
        assertThatThrownBy(() -> interpreter.execute(List.of(")")))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("unmatched closing parenthesis");
    }

    @Test
    void executeUnmatchedClosingParen2() {
        assertThatThrownBy(() -> interpreter.execute(List.of("(", ")", ")")))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("unmatched closing parenthesis");
    }

    @Test
    void executeUnmatchedOpeningParen() {
        assertThatThrownBy(() -> interpreter.execute(List.of("(")))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("unmatched opening parenthesis");
    }

    @Test
    void list() throws Exception {
        var ctx = interpreter.execute(List.<Object>of("(", "1", ")"));
        assertEquals(ctx, newContext(Stack.of(List.of("1"))));
    }

    @Test
    void nestedList() throws Exception {
        var ctx = interpreter.execute(List.<Object>of("(", "1", "(", ")", ")"));
        assertEquals(ctx, newContext(Stack.of(List.of("1", "(", ")"))));
    }

    @Test
    void multipleLists() throws Exception {
        var ctx = interpreter.execute(List.<Object>of("(", "1", ")", "(", "2", ")"));
        assertEquals(ctx, newContext(Stack.of(List.of("2"), List.of("1"))));
    }

    @Test
    void debug() throws Exception {
        var steps = interpreter.debug(List.of("(", "1", ")", "(", "2", ")"));
        assertThat(steps).containsExactly(
                new Interpreter.Step(List.of("(", "1", ")", "(", "2", ")"), newContext(Stack.of())),
                new Interpreter.Step(List.of( "(", "2", ")"), newContext(Stack.of(List.of("1")))),
                new Interpreter.Step(List.of(), newContext(Stack.of(List.of("2"), List.of("1"))))
        );
    }

    @Test
    void toStringShouldRenderProgram() {
        assertThat(interpreter.toString()).isEqualTo("Interpreter(9 words)");
    }

    @Test
    void typeSummaryString() {
        assertEquals(Interpreter.typeSummary(Stack.of("foo")), "[String]");
    }

    @Test
    void typeSummaryPrimitive() {
        assertEquals(Interpreter.typeSummary(Stack.of(42, 42.0, 42L)), "[Long,Double,Integer]");
    }

    @Test
    void typeSummaryListEmpty() {
        assertEquals(Interpreter.typeSummary(Stack.of(List.of())), "[List]");
    }

    @Test
    void typeSummaryList() {
        assertThat(Interpreter.typeSummary(Stack.of(List.of("a", "b")))).isEqualTo("[List]");
    }

    @Test
    void splitAndTrimShouldSplitByComma() throws Exception {
        var actual = Interpreter.splitAndTrim("a,b,c");
        assertThat(actual).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void splitAndTrimShouldTrimWhitespace() throws Exception {
        var actual = Interpreter.splitAndTrim("  a\n,\t\nb ,c");
        assertThat(actual).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void splitAndTrimShouldIgnoreEmptyStrings() throws Exception {
        var actual = Interpreter.splitAndTrim(", ,\t,\n\n,");
        assertThat(actual).isEqualTo(List.of());
    }

    @Test
    void splitAndTrimShouldNotSplitInsideParenthesis() throws Exception {
        var actual = Interpreter.splitAndTrim("a,(b,c),e");
        assertThat(actual).isEqualTo(List.of("a", "(b,c)", "e"));
    }

    @Test
    void splitAndTrimShouldNotSplitInsideSquareBrackets() throws Exception {
        var actual = Interpreter.splitAndTrim("a,[b,c],e");
        assertThat(actual).isEqualTo(List.of("a", "[b,c]", "e"));
    }

    @Test
    void splitAndTrimShouldNotSplitInsideCurlyBrackets() throws Exception {
        var actual = Interpreter.splitAndTrim("a,{b,c},e");
        assertThat(actual).isEqualTo(List.of("a", "{b,c}", "e"));
    }

    @Test
    void splitAndTrimShouldNotSplitAndTrimInsideNestedBlocks() throws Exception {
        var actual = Interpreter.splitAndTrim("a,(b, [{c}, d]),e");
        assertThat(actual).isEqualTo(List.of("a", "(b, [{c}, d])", "e"));
    }

    @Test
    void splitAndTrimWhenUnmatchedClosingParen() {
        assertThatThrownBy(() -> Interpreter.splitAndTrim("a)"))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("unmatched closing character ')'");
    }

    @Test
    void splitAndTrimWhenUnmatchedClosingParenOrphan() {
        assertThatThrownBy(() -> Interpreter.splitAndTrim("a[)"))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("unmatched closing character ')'");
    }

    @Test
    void splitAndTrimWhenUnmatchedOpeningParen() {
        assertThatThrownBy(() -> Interpreter.splitAndTrim("a("))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("unmatched opening character");
    }

    @Test
    void splitAndTrimList() throws Exception {
        var actual = Interpreter.splitAndTrim("a,(,b,c,),e");
        assertThat(actual).isEqualTo(List.of("a", "(", "b", "c", ")", "e"));

        actual = Interpreter.splitAndTrim("(,b,c,)");
        assertThat(actual).isEqualTo(List.of("(", "b", "c", ")"));
    }

    @Test
    void splitAndTrimListUnmatchedParen() throws Exception {
        // No error is thrown at this level for unmatched parens.
        var actual = Interpreter.splitAndTrim("(,b,c");
        assertThat(actual).isEqualTo(List.of("(", "b", "c"));

        actual = Interpreter.splitAndTrim("b,c,)");
        assertThat(actual).isEqualTo(List.of("b", "c", ")"));
    }

    private Context newContext(Stack vs) {
        return Context.initial(interpreter).withStack(vs);
    }

    static class TestWord extends AbstractWord {
        private final String value;

        TestWord(String name, String signature, String value) {
            super(name, signature);
            this.value = value;
        }

        @Override
        public boolean matches(Stack stack) {
            return true;
        }

        @Override
        public final Context execute(Context context) {
            return context.withStack(context.stack().push(value));
        }
    }

    static class Overloaded extends TestWord {
        private final boolean matches;

        Overloaded(String name, String value, boolean matches) {
            super(name, matches ? "* -- * v" : "exception", value);
            this.matches = matches;
        }

        @Override
        public boolean matches(Stack stack) {
            return matches;
        }
    }

    static class Unstable extends TestWord {
        Unstable() {
            super("unstable", "* -- * unstable", "unstable");
        }

        @Override
        public boolean isStable() {
            return false;
        }
    }
}
