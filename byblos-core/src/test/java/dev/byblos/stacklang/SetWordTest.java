package dev.byblos.stacklang;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.Set}.
 */
public class SetWordTest extends BaseWordTest {
    protected SetWordTest() {
        super(new StandardVocabulary.Set(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalWhenTwoValues() throws Exception {
        var ctx = matchAndEval("a,b");
        assertThat(ctx.stack()).isEmpty();
        assertThat(ctx.variables()).containsExactly(Map.entry("a", "b"));
    }

    @Test
    void evalWhenThreeValues() throws Exception {
        var ctx = matchAndEval("a,b,c");
        assertThat(ctx.stack()).containsExactly("a");
        assertThat(ctx.variables()).containsExactly(Map.entry("b", "c"));
    }

    @Test
    void doesNotMatchWhenEmpty() throws Exception {
        assertShouldNotMatch("");
    }

    @Test
    void doesNotMatchWhenOneValue() throws Exception {
        assertShouldNotMatch("a");
    }
}
