package dev.byblos.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.Clear}.
 */
public class ClearWordTest extends BaseWordTest {
    protected ClearWordTest() {
        super(new StandardVocabulary.Clear(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalWhenEmpty() throws Exception {
        var ctx = matchAndEval("");
        assertThat(ctx.stack()).isEmpty();
    }

    @Test
    void evalWhenOneValue() throws Exception {
        var ctx = matchAndEval("a");
        assertThat(ctx.stack()).isEmpty();
    }

    @Test
    void evalWhenTwoValues() throws Exception {
        var ctx = matchAndEval("a,b");
        assertThat(ctx.stack()).isEmpty();
    }
}
