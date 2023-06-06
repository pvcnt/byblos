package dev.byblos.core.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StandardVocabulary.Depth}.
 */
public class DepthWordTest extends BaseWordTest {
    protected DepthWordTest() {
        super(new StandardVocabulary.Depth(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalWhenSingleItemInTheStack() throws Exception {
        var ctx = matchAndEval("a");
        assertThat(ctx.stack()).containsExactly("1", "a");
    }

    @Test
    void evalWhenTwoItemsInTheStack() throws Exception {
        var ctx = matchAndEval("a,b");
        assertThat(ctx.stack()).containsExactly("2", "b", "a");
    }

    @Test
    void evalWhenFiveItemsInTheStack() throws Exception {
        var ctx = matchAndEval("a,b,c,d,e");
        assertThat(ctx.stack()).containsExactly("5", "e", "d", "c", "b", "a");
    }
}
