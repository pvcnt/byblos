package dev.byblos.core.stacklang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link StandardVocabulary.Pick}.
 */
public class PickWordTest extends BaseWordTest {
    protected PickWordTest() {
        super(new StandardVocabulary.Pick(), new Interpreter(StandardVocabulary.INSTANCE.allWords()));
    }

    @Test
    void evalPickItemZeroFromStackOfOneItem() throws Exception {
        var ctx = matchAndEval("a,0");
        assertThat(ctx.stack()).containsExactly("a", "a");
    }

    @Test
    void evalPickItemZeroFromStackOfTwoItems() throws Exception {
        var ctx = matchAndEval("a,b,0");
        assertThat(ctx.stack()).containsExactly("b", "b", "a");
    }

    @Test
    void evalPickItemOneFromStackOfTwoItems() throws Exception {
        var ctx = matchAndEval("a,b,1");
        assertThat(ctx.stack()).containsExactly("a", "b", "a");
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

    @Test
    void throwsWhenOutOfBounds() {
        assertThatThrownBy(() -> matchAndEval("a,1"))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("cannot pick item 1 from stack of size 1");
        assertThatThrownBy(() -> matchAndEval("a,b,3"))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("cannot pick item 3 from stack of size 2");
    }
}
