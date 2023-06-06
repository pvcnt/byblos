package dev.byblos.core.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.Rot}.
 */
public class RotWordTest extends BaseWordTest {
    protected RotWordTest() {
        super(new StandardVocabulary.Rot(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
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
        assertThat(ctx.stack()).containsExactly("a", "e", "d", "c", "b");
    }

    @Test
    void doesNotMatchWhenEmpty() throws Exception {
        assertShouldNotMatch("");
    }
}
