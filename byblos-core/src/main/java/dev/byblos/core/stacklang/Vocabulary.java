package dev.byblos.core.stacklang;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Vocabulary {

    String name();

    List<Vocabulary> dependsOn();

    List<Word> words();

    /**
     * Return a flattened list of all words from this vocabulary plus words from all dependencies.
     */
    default List<Word> allWords() {
        return Stream.concat(dependsOn().stream().flatMap(v -> v.allWords().stream()), words().stream()).collect(Collectors.toList());
    }

    /**
     * Return a flattened list of all dependency vocabularies.
     */
    default List<Vocabulary> dependencies() {
        return dependsOn().stream()
                .flatMap(d -> Stream.concat(Stream.of(d), d.dependencies().stream()))
                .collect(Collectors.toList());
    }
}
