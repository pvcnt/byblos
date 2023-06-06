package dev.byblos.core.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.Swap}.
 */
public class SwapWordTest extends BaseWordTest {
    protected SwapWordTest() {
        super(new StandardVocabulary.Swap(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalWhenTwoItemsInTheStack() throws Exception {
        var ctx = matchAndEval("a,b");
        assertThat(ctx.stack()).containsExactly("a", "b");
    }

    @Test
    void evalWhenFiveItemsInTheStack() throws Exception {
        var ctx = matchAndEval("a,b,c,d,e");
        assertThat(ctx.stack()).containsExactly("d", "e", "c", "b", "a");
    }

    @Test
    void doesNotMatchWhenEmpty() throws Exception {
        assertShouldNotMatch("");
    }

    @Test
    void doesNotMatchWhenSingleItemInTheStack() throws Exception {
        assertShouldNotMatch("a");
    }
}
