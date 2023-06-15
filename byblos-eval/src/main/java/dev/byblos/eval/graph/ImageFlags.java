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

    public abstract Map<Integer, Axis> axes();

    @Value.Default
    public boolean axisPerLine() {
        return false;
    }

    @Value.Default
    public boolean showLegend() {
        return true;
    }

    @Value.Default
    public boolean showLegendStats() {
        return true;
    }

    @Value.Default
    public boolean showOnlyGraph() {
        return false;
    }

    @Value.Default
    public VisionType vision() {
        return VisionType.normal;
    }

    public abstract String palette();

    public abstract String theme();

    @Value.Default
    public Layout layout() {
        return Layout.CANVAS;
    }

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
