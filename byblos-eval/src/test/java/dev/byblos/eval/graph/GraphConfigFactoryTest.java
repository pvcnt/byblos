package dev.byblos.eval.graph;

import com.google.common.collect.Multimaps;
import com.typesafe.config.ConfigFactory;
import dev.byblos.chart.model.AutoDataBound;
import dev.byblos.chart.model.AutoStyleBound;
import dev.byblos.chart.model.ExplicitBound;
import dev.byblos.chart.model.PlotBound;
import dev.byblos.core.model.ConstantExpr;
import dev.byblos.core.model.StyleExpr;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GraphConfigFactory}.
 */
public class GraphConfigFactoryTest {
    private final GraphConfigFactory factory = new GraphConfigFactory(DefaultSettings.fromConfig(ConfigFactory.load()));

    @Test
    void simpleExpr() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const")));

        assertThat(cfg.parsedQuery()).containsExactly(new StyleExpr(new ConstantExpr(42), Map.of()));
        assertThat(lowerBound(cfg)).isEqualTo(AutoStyleBound.INSTANCE);
        assertThat(cfg.flags().hints()).isEmpty();
    }

    @Test
    void emptyTitle() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "title", "")));
        assertThat(cfg.flags().title()).isEmpty();
    }

    @Test
    void withTitle() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "title", "foo")));
        assertThat(cfg.flags().title()).contains("foo");
    }

    @Test
    void emptyYLabel() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "ylabel", "")));
        assertThat(cfg.flags().axes().get(0).yLabel()).isEmpty();
    }

    @Test
    void withYLabel() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "ylabel", "foo")));
        assertThat(cfg.flags().axes().get(0).yLabel()).contains("foo");
    }

    @Test
    void lowerBoundExplicit() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "l", "0")));
        assertThat(lowerBound(cfg)).isEqualTo(new ExplicitBound(0d));
    }

    @Test
    void lowerBoundAutoData() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "l", "auto-data")));
        assertThat(lowerBound(cfg)).isEqualTo(AutoDataBound.INSTANCE);
    }

    @Test
    void lowerBoundAutoStyle() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "l", "auto-style")));
        assertThat(lowerBound(cfg)).isEqualTo(AutoStyleBound.INSTANCE);
    }

    @Test
    void emptyHints() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "hints", "")));
        assertThat(cfg.flags().hints()).isEmpty();
    }

    @Test
    void singleHint() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "hints", "a")));
        assertThat(cfg.flags().hints()).containsExactly("a");
    }

    @Test
    void multipleHints() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "hints", "a,b,c")));
        assertThat(cfg.flags().hints()).containsExactly("a", "b", "c");
    }

    @Test
    void multipleHintsMessy() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const", "hints", "a,b, a ,b,b,b,c")));
        assertThat(cfg.flags().hints()).containsExactly("a", "b", "c");
    }

    @Test
    void useCorsOriginAsDefaultId() {
        var cfg = factory.toGraphConfig(newGraphRequest(Map.of("q", "42,:const"), Map.of("origin", "https://foo.prometheus.io")));
        assertThat(cfg.id()).isEqualTo("foo.prometheus.io");
    }

    @Test
    void explicitIdParameterTakesPrecedenceOverCorsOrigin() {
        var cfg = factory.toGraphConfig(newGraphRequest(
                Map.of("q", "42,:const", "id", "bar"),
                Map.of("origin", "https://foo.prometheus.io")));
        assertThat(cfg.id()).isEqualTo("bar");
    }

    private static GraphRequest newGraphRequest(Map<String, String> params) {
        return newGraphRequest(params, Map.of());
    }

    private static GraphRequest newGraphRequest(Map<String, String> params, Map<String, String> headers) {
        return new GraphRequest("/api/v1/graph", Multimaps.forMap(params), Multimaps.forMap(headers));
    }

    private static PlotBound lowerBound(GraphConfig config) {
        return config.flags().axes().get(0).newPlotDef(List.of(), false).lower();
    }
}
