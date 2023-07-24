package dev.byblos.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ArrayTimeSeq}.
 */
public class ArrayTimeSeqTest {

    @Test
    void equals() {
        EqualsVerifier
                .forClass(ArrayTimeSeq.class)
                // end is a computed field, it is expected it will not be used.
                .withIgnoredFields("end")
                .verify();
    }
}
