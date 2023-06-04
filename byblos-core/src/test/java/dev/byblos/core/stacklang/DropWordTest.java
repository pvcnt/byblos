package dev.byblos.core.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.Drop}.
 */
public class DropWordTest extends BaseWordTest {
    protected DropWordTest() {
        super(new StandardVocabulary.Drop(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalWhenOneValue() throws Exception {
        var ctx = matchAndEval("a");
        assertThat(ctx.stack()).isEmpty();
    }

    @Test
    void evalWhenTwoValues() throws Exception {
        var ctx = matchAndEval("a,b");
        assertThat(ctx.stack()).containsExactly("a");
    }

    @Test
    void doesNotMatchWhenEmpty() throws Exception {
        assertShouldNotMatch("");
    }
}
