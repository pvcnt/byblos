package dev.byblos.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.Get}.
 */
public class GetWordTest extends BaseWordTest {
    protected GetWordTest() {
        super(new StandardVocabulary.Get(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalWhenCalledOnce() throws Exception {
        var ctx = matchAndEval("a,b,:set,a,b,c,a");
        assertThat(ctx.stack()).containsExactly("b", "c", "b", "a");
    }

    @Test
    void evalWhenCalledTwice() throws Exception {
        var ctx = matchAndEval("a,b,:set,a,c,:set,a");
        assertThat(ctx.stack()).containsExactly("c");
    }

    @Test
    void doesNotMatchWhenEmpty() throws Exception {
        assertShouldNotMatch("");
    }
}
