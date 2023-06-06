package dev.byblos.core.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.ReverseRot}.
 */
public class ReverseRotWordTest extends BaseWordTest {
    protected ReverseRotWordTest() {
        super(new StandardVocabulary.ReverseRot(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalWhenSingleItemInTheStack() throws Exception {
        var ctx = matchAndEval("a");
        assertThat(ctx.stack()).containsExactly("a");
    }

    @Test
    void evalWhenTwoItemsInTheStack() throws Exception {
        var ctx = matchAndEval("a,b");
        assertThat(ctx.stack()).containsExactly("a", "b");
    }

    @Test
    void evalWhenFiveItemsInTheStack() throws Exception {
        var ctx = matchAndEval("a,b,c,d,e");
        assertThat(ctx.stack()).containsExactly("d", "c", "b", "a", "e");
    }

    @Test
    void doesNotMatchWhenEmpty() throws Exception {
        assertShouldNotMatch("");
    }
}
