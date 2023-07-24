package dev.byblos.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.Over}.
 */
public class OverWordTest extends BaseWordTest {
    protected OverWordTest() {
        super(new StandardVocabulary.Over(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalWhenTwoItemsInTheStack() throws Exception {
        var ctx = matchAndEval("a,b");
        assertThat(ctx.stack()).containsExactly("a", "b", "a");
    }

    @Test
    void evalWhenThreeItemsInTheStack() throws Exception {
        var ctx = matchAndEval("a,b,c");
        assertThat(ctx.stack()).containsExactly("b", "c", "b", "a");
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
