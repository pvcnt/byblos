package dev.byblos.core.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.Drop}.
 */
public class DupWordTest extends BaseWordTest {
    protected DupWordTest() {
        super(new StandardVocabulary.Dup(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalWhenOneValue() throws Exception {
        var ctx = matchAndEval("a");
        assertThat(ctx.stack()).containsExactly("a", "a");
    }

    @Test
    void evalWhenTwoValues() throws Exception {
        var ctx = matchAndEval("a,b");
        assertThat(ctx.stack()).containsExactly("b", "b", "a");
    }

    @Test
    void doesNotMatchWhenEmpty() throws Exception {
        assertShouldNotMatch("");
    }
}
