package dev.byblos.chart.model;

import dev.byblos.chart.GraphConstants;
import dev.byblos.chart.graphics.ChartSettings;
import dev.byblos.chart.graphics.Theme;
import dev.byblos.core.model.CollectorStats;
import dev.byblos.core.model.SummaryStats;
import org.immutables.value.Value;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

@Value.Immutable
@Value.Style(overshadowImplementation = true)
public abstract class GraphDef {

    /**
     * Returns true if the hints indicate that the ambiguous multi-Y mode should be used.
     */
    public static boolean ambiguousMultiY(Set<String> hints) {
        return hints.contains("ambiguous-multi-y");
    }

    @Value.Check
    protected void check() {
        checkArgument(!timezones().isEmpty(), "at least one timezone must be specified for the chart");
    }

    /**
     * Return start time (inclusive) for the first datapoint.
     */
    public abstract Instant startTime();

    /**
     * Return end time (exclusive) for the last datapoint.
     */
    public abstract Instant endTime();

    /**
     * Return step size for each datapoint.
     */
    @Value.Default
    public long step() {
        return 60000;
    }

    /**
     * Return width in pixels for the chart area. This excludes axes and other padding. The final image
     * size will get calculated using this width as a starting point.
     */
    @Value.Default
    public int width() {
        return 400;
    }

    /**
     * Return height in pixels for the chart area. This excludes the title, time axis, legend, etc. The
     * final image size will get calculated using this height as a starting point.
     */
    @Value.Default
    public int height() {
        return 200;
    }

    /**
     * Return layout mode to use for rendering the image. public is CANVAS.
     */
    @Value.Default
    public Layout layout() {
        return Layout.CANVAS;
    }

    /**
     * Return plot definitions. Each plot has its own y-axis and set of lines.
     */
    public abstract List<PlotDef> plots();

    public PlotDef plot(int i) {
        PlotDef plot = plots().get(i);
        if (null == plot) {
            throw new NoSuchElementException();
        }
        return plot;
    }

    /**
     * Return whether to show only the chart without other details like axes, legends, labels, etc.
     */
    @Value.Default
    public boolean onlyGraph() {
        return false;
    }

    /**
     * Used to provide metadata for how the graph definition was created. For example the uri
     * input by the user.
     */
    public abstract Optional<String> source();

    /**
     * Return the title of the graph.
     */
    public abstract Optional<String> title();

    /**
     * Return how long it took to load the data for the chart in milliseconds.
     */
    @Value.Default
    public long loadTime() {
        return -1;
    }

    /**
     * Return stats on how much data was processed to render the chart.
     */
    @Value.Default
    public CollectorStats stats() {
        return CollectorStats.EMPTY;
    }

    /**
     * Return warnings to display to the user.
     */
    public abstract List<String> warnings();

    /**
     * Return arbitrary hints passed to the rendering engine to adjust behavior.
     */
    public abstract Set<String> renderingHints();

    /**
     * Return which theme to use for the chart, typically light or dark mode.
     */
    public abstract String themeName();

    /**
     * Return the pattern used for formatting the number values in text based outputs.
     */
    @Value.Default
    public String numberFormat() {
        return "%f";
    }

    /**
     * Total number of lines for all plots.
     */
    public int numLines() {
        return plots().stream().mapToInt(p -> p.data().size()).sum();
    }

    /**
     * Return timezones to show as time axes on the chart. The first time zone in the list will be the
     * primary used when displaying time stamps or for formats that don't support multiple time
     * zone rendering.
     */
    @Value.Default
    public List<ZoneId> timezones() {
        return List.of(ZoneOffset.UTC);
    }

    /**
     * Return the primary timezone to use for the graph.
     */
    public ZoneId timezone() {
        return timezones().get(0);
    }

    /**
     * Return the color theme to use for the graph.
     */
    public Theme theme() {
        return ChartSettings.theme(themeName());
    }

    /**
     * Return how to show the legend when rendering the graph.
     */
    @Value.Default
    public LegendType legendType() {
        return LegendType.LABELS_WITH_STATS;
    }

    /**
     * Returns true if text should be shown.
     */
    public boolean showText() {
        return width() >= ChartSettings.minWidthForText;
    }

    /**
     * Returns true if the legend should be shown.
     */
    public boolean showLegend() {
        return !onlyGraph() && legendTypeForLayout() != LegendType.OFF && showText();
    }

    /**
     * Returns true if legend stats should be shown.
     */
    public boolean showLegendStats() {
        return !onlyGraph() && legendTypeForLayout() == LegendType.LABELS_WITH_STATS;
    }

    /**
     * Returns true if multi-Y axis should use the color from the first line on the
     * axis. This is done for static images to provide a visual cue for what axis is
     * associated with a given line. That behavior can be disabled by setting the
     * rendering hint `ambiguous-multi-y` to indicate that the axis should determine the
     * color from the graph theme instead.
     */
    public boolean useLineColorForMultiY() {
        return plots().size() > 1 && !GraphDef.ambiguousMultiY(renderingHints());
    }

    public LegendType legendTypeForLayout() {
        return (layout().isFixedHeight()) ? LegendType.OFF : legendType();
    }

    /**
     * Helper to map the lines that are part of the graph.
     */
    public GraphDef adjustLines(Function<LineDef, LineDef> f) {
        return adjustPlots(plot -> {
            var newData = plot.data().stream().map(x -> x instanceof LineDef ? f.apply((LineDef) x) : x).collect(Collectors.toList());
            return plot.toBuilder().data(newData).build();
        });
    }

    /**
     * Helper to map the plots that are part of the graph.
     */
    public GraphDef adjustPlots(Function<PlotDef, PlotDef> f) {
        return toBuilder().plots(plots().stream().map(f).collect(Collectors.toList())).build();
    }

    public GraphDef warn(String warning) {
        var newWarnings = Stream.concat(warnings().stream(), Stream.of(warning)).collect(Collectors.toList());
        return toBuilder().warnings(newWarnings).build();
    }

    /**
     * Set the vision type for the graph to simulate types of color blindness.
     */
    public GraphDef withVisionType(VisionType vt) {
        return adjustPlots(plot -> {
            var newData = plot.data().stream().map(d -> d.withColor(vt.convert(d.color()))).collect(Collectors.toList());
            return plot.toBuilder()
                    .data(newData)
                    .axisColor(plot.axisColor().map(vt::convert))
                    .build();
        });
    }

    /**
     * Return a new graph definition with the line stats filled in.
     */
    public GraphDef computeStats() {
        var s = startTime().toEpochMilli();
        var e = endTime().toEpochMilli();
        return adjustLines(line -> {
            var stats = SummaryStats.fromData(line.data().data(), s, e);
            return ImmutableLineDef.builder().from(line).legendStats(stats).build();
        });
    }

    /**
     * Convert the defintion from a single axis to using one per line in the chart.
     */
    public GraphDef axisPerLine() {
        if (plots().size() > 1) {
            return warn("axisPerLine cannot be used with explicit multi axis");
        }
        var plot = plots().get(0);
        var size = plot.data().size();
        if (size > GraphConstants.MaxYAxis) {
            var msg = String.format("Too many Y-axes, %s > %s, axis per line disabled.", size, GraphConstants.MaxYAxis);
            return warn(msg);
        }
        var useLineColor = !GraphDef.ambiguousMultiY(renderingHints());
        var newPlots = plot.data().stream().map(d -> {
            var axisColor = useLineColor ? Optional.of(d.color()) : Optional.<Color>empty();
            return plot.toBuilder().data(List.of(d)).axisColor(axisColor).build();
        }).collect(Collectors.toList());
        return toBuilder().plots(newPlots).build();
    }

    /**
     * Normalize the definition so it can be reliably compared. Mostly used for test cases.
     */
    public GraphDef normalize() {
        if (useLineColorForMultiY()) {
            // public behavior for multi-Y is to make the axis color match the data for the plots
            return adjustPlots(plot -> {
                if (plot.axisColor().isEmpty()) {
                    var axisColor = Optional.ofNullable(plot.data().get(0)).map(DataDef::color);
                    return axisColor.map(c -> plot.toBuilder().axisColor(c).build()).orElseGet(() -> plot.normalize(theme()));
                }
                return plot;
            }).bounded();
        }
        return adjustPlots(p -> p.normalize(theme())).bounded();
    }

    /**
     * Return a new graph definition with the lines bounded.
     */
    public GraphDef bounded() {
        var s = startTime().toEpochMilli();
        var e = endTime().toEpochMilli();
        return adjustLines(line -> {
            var seq = line.data().data().bounded(s, e);
            return line.toBuilder().data(line.data().withData(seq)).build();
        });
    }

    public ImmutableGraphDef.Builder toBuilder() {
        return ImmutableGraphDef.builder().from(this);
    }
}
