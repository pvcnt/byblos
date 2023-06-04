package dev.byblos.eval.graph;

import dev.byblos.chart.model.*;
import dev.byblos.core.util.Strings;
import dev.byblos.core.util.UnitPrefix;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value.Immutable
@Value.Style(overshadowImplementation = true)
public abstract class Axis {
    public abstract Optional<String> upper();

    public abstract Optional<String> lower();

    public abstract Optional<String> scale();

    public abstract boolean stack();

    public abstract Optional<String> yLabel();

    public abstract Optional<String> tickLabels();

    public abstract Optional<String> palette();

    public abstract Optional<String> sort();

    public abstract Optional<String> order();

    /**
     * Return the default line style to use.
     */
    public LineStyle lineStyle() {
        return stack() ? LineStyle.STACK : LineStyle.LINE;
    }

    public boolean useDescending() {
        return order().stream().anyMatch("desc"::equals);
    }

    @Value.Derived
    public TickLabelMode tickLabelMode() {
        return tickLabels().map(TickLabelMode::fromString).orElse(TickLabelMode.DECIMAL);
    }

    @Value.Derived
    public Function<Double, String> statFormatter() {
        if (tickLabelMode() == TickLabelMode.BINARY) {
            return v -> UnitPrefix.binary(v).format(v);
        }
        return v -> UnitPrefix.decimal(v).format(v);
    }

    public PlotDef newPlotDef(List<DataDef> data, boolean multiY) {
        return ImmutablePlotDef.builder()
                .data(data)
                .lower(lower().map(PlotBound::fromString).orElse(AutoStyleBound.INSTANCE))
                .upper(upper().map(PlotBound::fromString).orElse(AutoStyleBound.INSTANCE))
                .yLabel(yLabel().map(s -> Strings.substitute(s, getAxisTags(data))))
                .scale(scale().map(Scale::fromString).orElse(Scale.LINEAR))
                .axisColor((multiY && !data.isEmpty()) ? Optional.of(data.get(0).color()) : Optional.empty())
                .tickLabelMode(tickLabelMode())
                .build();
    }

    static Map<String, String> getAxisTags(List<DataDef> data) {
        // Returns the list of tags common to all line plots.
        return data.stream()
                .filter(d -> d instanceof LineDef)
                .map(d -> ((LineDef) d).data().tags())
                .reduce(Axis::intersect)
                .orElse(Map.of());
    }

    private static Map<String, String> intersect(Map<String, String> a, Map<String, String> b) {
        return a.entrySet().stream()
                .filter(e -> Objects.equals(e.getValue(), b.get(e.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
