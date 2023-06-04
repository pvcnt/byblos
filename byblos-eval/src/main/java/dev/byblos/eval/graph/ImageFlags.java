package dev.byblos.eval.graph;

import dev.byblos.chart.model.Layout;
import dev.byblos.chart.model.LegendType;
import dev.byblos.chart.model.VisionType;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
public abstract class ImageFlags {
    public abstract Optional<String> title();

    public abstract int width();

    public abstract int height();

    public abstract double zoom();

    public abstract Map<Integer, Axis> axes();

    public abstract boolean axisPerLine();

    public abstract boolean showLegend();

    public abstract boolean showLegendStats();

    public abstract boolean showOnlyGraph();

    public abstract VisionType vision();

    public abstract String palette();

    public abstract String theme();

    public abstract Layout layout();

    public abstract Set<String> hints();

    public LegendType legendType() {
        if (!showLegend()) {
            return LegendType.OFF;
        } else if (!showLegendStats()) {
            return LegendType.LABELS_ONLY;
        }
        return LegendType.LABELS_WITH_STATS;
    }
}
