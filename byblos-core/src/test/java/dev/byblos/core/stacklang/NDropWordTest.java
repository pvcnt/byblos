package dev.byblos.core.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.NDrop}.
 */
public class NDropWordTest extends BaseWordTest {
    protected NDropWordTest() {
        super(new StandardVocabulary.NDrop(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalNDropZeroItems() throws Exception {
        var ctx = matchAndEval("a,0");
        assertThat(ctx.stack()).containsExactly("a");
    }

    @Test
    void evalNDropOneItem() throws Exception {
        var ctx = matchAndEval("a,b,1");
        assertThat(ctx.stack()).containsExactly("a");
    }

    @Test
    void evalNDropAllItems() throws Exception {
        var ctx = matchAndEval("a,b,2");
        assertThat(ctx.stack()).isEmpty();
    }

    @Test
    void doesNotMatchWhenEmpty() throws Exception {
        assertShouldNotMatch("");
    }

    @Test
    void doesNotMatchWhenNotAnInt() throws Exception {
        assertShouldNotMatch("a");
        assertShouldNotMatch("a,b");
    }
}
