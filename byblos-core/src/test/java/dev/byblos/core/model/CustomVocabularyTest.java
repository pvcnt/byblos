package dev.byblos.core.model;

import com.typesafe.config.ConfigFactory;
import dev.byblos.core.stacklang.Interpreter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomVocabularyTest {
    @Test
    void customWord() throws Exception {
        var config = ConfigFactory.parseString("byblos.core.vocabulary { \n" +
                "  words = [ \n" +
                "    { \n" +
                "      name = \"dup2\" \n" +
                "      body = \":dup,:dup\" \n" +
                "      examples = [] \n" +
                "      } \n" +
                "    ] \n" +
                "  } \n");

        var vocab = new CustomVocabulary(config);
        var interpreter = new Interpreter(vocab.allWords());

        var result = interpreter.execute("2,:dup2");
        assertThat(result.stack().stream()).containsExactly("2", "2", "2");
    }
}
