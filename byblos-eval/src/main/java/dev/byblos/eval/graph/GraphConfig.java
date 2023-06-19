package dev.byblos.eval.graph;

import dev.byblos.chart.GraphEngine;
import dev.byblos.chart.model.GraphDef;
import dev.byblos.chart.model.ImmutableGraphDef;
import dev.byblos.chart.model.PlotDef;
import dev.byblos.core.model.EvalContext;
import dev.byblos.core.model.StyleExpr;
import dev.byblos.core.util.Features;
import dev.byblos.core.util.Pair;
import dev.byblos.core.util.Step;
import dev.byblos.core.util.Strings;
import org.immutables.value.Value;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Value.Immutable
@Value.Style(overshadowImplementation = true)
public abstract class GraphConfig {

    public abstract String query();

    public abstract List<StyleExpr> parsedQuery();

    public abstract Optional<Throwable> parseException();

    public abstract DefaultSettings settings();

    public abstract Optional<String> start();

    public abstract Optional<String> end();

    public abstract Optional<String> step();

    public abstract List<String> timezones();

    public abstract ImageFlags flags();

    @Value.Default
    public String format() {
        return "png";
    }

    @Value.Default
    public String id() {
        return "default";
    }

    @Value.Default
    public Features features() {
        return Features.STABLE;
    }

    @Value.Default
    public boolean browser() {
        return false;
    }

    public abstract String uri();

    public GraphEngine engine() {
        var engine = settings().engines().get(format());
        if (null == engine) {
            throw new IllegalArgumentException(String.format("cannot handle format '%s'", format()));
        }
        return engine;
    }

    public String contentType() {
        var contentType = settings().contentTypes().get(format());
        if (null == contentType) {
            throw new IllegalArgumentException(String.format("cannot handle format '%s'", format()));
        }
        return contentType;
    }

    public ZoneId timezone() {
        return timeZoneIds().get(0);
    }

    @Value.Derived
    public List<ZoneId> timeZoneIds() {
        return getTimeZoneIds(settings(), timezones());
    }

    public static List<ZoneId> getTimeZoneIds(DefaultSettings settings, List<String> timezones) {
        var zoneStrs = timezones.isEmpty() ? List.of(settings.timezone()) : timezones;
        return zoneStrs.stream().map(ZoneId::of).collect(Collectors.toList());
    }

    @Value.Derived
    public EvalContext evalContext() {
        var timeRange = finalTimeRange();
        return new EvalContext(timeRange.first().toEpochMilli(), timeRange.second().toEpochMilli() + stepSize(), stepSize());
    }

    @Value.Derived
    public Pair<Instant, Instant> resolvedTimeRange() {
        return Strings.timeRange(start().orElse(settings().startTime()), end().orElse(settings().endTime()), timezone());
    }

    /**
     * Returns the effective step size for the graph after adjusting based on the size and time window.
     */
    @Value.Derived
    public long stepSize() {
        // Input step size rounded if necessary to a supported step.
        var roundedStepSize = step()
                .map(s -> Step.round(settings().stepSize(), Strings.parseDuration(s).toMillis()))
                .orElse(settings().stepSize());

        var datapointWidth = Math.min(settings().maxDatapoints(), flags().width());
        var timeRange = resolvedTimeRange();
        return Step.compute(roundedStepSize, datapointWidth, timeRange.first().toEpochMilli(), timeRange.second().toEpochMilli());
    }

    /**
     * Returns final start and end time rounded to step boundaries.
     */
    @Value.Derived
    public Pair<Instant, Instant> finalTimeRange() {
        var timeRange = resolvedTimeRange();
        var rs = roundToStep(timeRange.first(), stepSize());
        var re = roundToStep(timeRange.second(), stepSize());
        var adjustedStart = rs.equals(re) ? rs.minusMillis(stepSize()) : rs;
        return new Pair<>(adjustedStart, re);
    }

    private static Instant roundToStep(Instant i, long stepSize) {
        return Instant.ofEpochMilli(i.toEpochMilli() / stepSize * stepSize);
    }

    public long startMillis() {
        return finalTimeRange().first().toEpochMilli() + stepSize();
    }

    public long endMillis() {
        return finalTimeRange().second().toEpochMilli() + stepSize();
    }

    public GraphDef newGraphDef(List<PlotDef> plots, Duration fetchTime, List<String> warnings) {
        var timeRange = finalTimeRange();
        var graphTags = Axis.getAxisTags(plots.stream().flatMap(p -> p.data().stream()).collect(Collectors.toList()));
        var title = flags().title().map(s -> Strings.substitute(s, graphTags));
        var graphDef = ImmutableGraphDef.builder()
                .startTime(timeRange.first().plusMillis(stepSize()))
                .endTime(timeRange.second().plusMillis(stepSize()))
                .title(title)
                .timezones(timeZoneIds())
                .step(stepSize())
                .width(flags().width())
                .height(flags().height())
                .layout(flags().layout())
                .legendType(flags().legendType())
                .onlyGraph(flags().showOnlyGraph())
                .themeName(flags().theme())
                .plots(plots)
                .fetchTime(fetchTime)
                .source(settings().metadataEnabled() ? Optional.of(uri()) : Optional.empty())
                .warnings(warnings)
                .renderingHints(flags().hints())
                .build();
        return flags().axisPerLine() ? useAxisPerLine(graphDef) : graphDef;
    }

    private GraphDef useAxisPerLine(GraphDef gdef) {
        var graphDef = gdef.axisPerLine();
        var multiY = graphDef.useLineColorForMultiY();
        var plots = IntStream.range(0, graphDef.plots().size())
                .mapToObj(i -> flags().axes().get(i).newPlotDef(graphDef.plots().get(i).data(), multiY))
                .collect(Collectors.toList());
        return graphDef.toBuilder().plots(plots).build();
    }
}
