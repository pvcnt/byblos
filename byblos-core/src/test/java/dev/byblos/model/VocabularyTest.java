package dev.byblos.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VocabularyTest {
    @Test
    void wordEquality() {
        for (var word : StyleVocabulary.INSTANCE.allWords()) {
            assertThat(word).isEqualTo(word);
        }
    }
}
