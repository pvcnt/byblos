package dev.byblos.core.stacklang;

import dev.byblos.core.util.Features;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link StandardVocabulary.Freeze}.
 */
public class FreezeTest {
    private final Interpreter interpreter = new Interpreter(StandardVocabulary.INSTANCE.allWords());

    @Test
    void basicOperation() throws Exception {
        var context = interpreter.execute("a,b,c,:freeze");
        assertThat(context.stack()).containsExactly("c", "b", "a");
        assertThat(context.frozenStack()).isEmpty();
    }

    @Test
    void frozenStackIsIsolated() throws Exception {
        var context = interpreter.execute("a,b,c,:freeze,d,e,f,:clear");
        assertThat(context.stack()).containsExactly("c", "b", "a");
        assertThat(context.frozenStack()).isEmpty();
    }

    @Test
    void variablesAreCleared() {
        assertThatThrownBy(() -> interpreter.execute("foo,1,:set,:freeze,foo,:get"))
                .isInstanceOf(InvalidSyntaxException.class)
                .hasMessage("unknown variable 'foo'");
    }

    @Test
    void originalVariablesArePreserved() throws Exception {
        var vars = Map.<String, Object>of("foo", "original", "bar", "2");
        var context = interpreter.execute("foo,1,:set,:freeze,foo,:get,bar,:get", vars, Features.STABLE);
        assertThat(context.stack()).containsExactly("2", "original");
    }

    @Test
    void multipleFreezeOperations() throws Exception {
        var context = interpreter.execute("a,b,c,:freeze,d,e,f,:freeze,g,h,i,:freeze,j,k,l,:clear");
        assertThat(context.stack()).containsExactly("i", "h", "g", "f", "e", "d", "c", "b", "a");
        assertThat(context.frozenStack()).isEmpty();
    }

    /* TODO: test("freeze works with macros") {
        // Before macros would force unfreeze after execution
        val context = interpreter.execute("a,b,:freeze,d,e,:2over,:clear")
        assertEquals(context.stack, List("b", "a"))
        assert(context.frozenStack.isEmpty)
    }*/
}
