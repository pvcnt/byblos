package dev.byblos.model;

import com.typesafe.config.Config;
import dev.byblos.stacklang.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Vocabulary that allows custom extension operations to be loaded from the
 * config.
 * <p>
 * **Words**
 * <p>
 * Custom words can be defined using an expression. These are typically used
 * by the operators to provide common helper functions.
 * <p>
 * ```
 * words = [
 * {
 * name = "square"
 * body = ":dup,:mul"
 * examples = ["2"]
 * }
 * ]
 * ```
 * <p>
 * The supported fields are:
 * <p>
 * - `name`: operation name, when the user calls the operation they will use
 * `:\$name`.
 * - `body`: expression that is executed for this operation.
 * - `examples`: set of example stacks that can be used as input to the operator
 * to show how it works.
 */
public final class CustomVocabulary implements Vocabulary {
    private final List<Word> words;

    /**
     * Constructor
     *
     * @param config Config instance to use for loading the custom operations. The settings
     *               will be loaded from the `byblos.vocabulary` block.
     */
    public CustomVocabulary(Config config) {
        var vocab = config.getConfig("byblos.vocabulary");
        words = loadCustomWords(vocab.getConfigList("words"));
    }

    @Override
    public String name() {
        return "custom";
    }

    @Override
    public List<Vocabulary> dependsOn() {
        return List.of(StyleVocabulary.INSTANCE);
    }

    @Override
    public List<Word> words() {
        return words;
    }

    private List<Word> loadCustomWords(List<? extends Config> configs) {
        return configs.stream().map(cfg -> {
            var name = cfg.getString("name");
            try {
                var body = Interpreter.splitAndTrim(cfg.getString("body"));
                return new Macro(name, body);
            } catch (InvalidSyntaxException e) {
                throw new IllegalArgumentException("Cannot load macro [" + name + "]", e);
            }
        }).collect(Collectors.toList());
    }
}
