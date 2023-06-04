package dev.byblos.core.stacklang;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BaseWordTest {
    private final Word word;
    private final Interpreter interpreter;

    protected BaseWordTest(Word word, Interpreter interpreter) {
        this.word = word;
        this.interpreter = interpreter;
    }

    protected final Context matchAndEval(String program) throws Exception {
        assertTrue(word.matches(interpreter.execute(program).stack()));
        var ctx = interpreter.execute(program);
        return word.execute(ctx);
    }

    protected final void assertShouldNotMatch(String program) throws Exception {
        assertFalse(word.matches(interpreter.execute(program).stack()));
    }
}
