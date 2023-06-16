package dev.byblos.eval.graph;

import com.typesafe.config.ConfigFactory;
import dev.byblos.eval.db.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Grapher}.
 */
public class GrapherTest {
    private DefaultSettings defaultSettings;
    private Grapher grapher;

    @BeforeEach
    void setUp() {
        defaultSettings = DefaultSettings.fromConfig(ConfigFactory.load());
        Database database = (context, expr) -> List.of();
        grapher = new Grapher(defaultSettings, database);
    }

    @Test
    void emptyQueryMustRenderError() {
        var flags = ImmutableImageFlags.builder().width(600).height(400).palette("armytage").theme("light").build();
        var config = ImmutableGraphConfig.builder().query("").flags(flags).uri("").settings(defaultSettings).format("csv").build();

        var result = grapher.evalAndRender(config);

        assertThat(result.code()).isEqualTo(GraphResult.Code.USER_ERROR);
        assertThat(new String(result.data())).contains("expression generated no lines");
    }
}
