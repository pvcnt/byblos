package dev.byblos.eval.graph;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dev.byblos.chart.GraphConstants;
import dev.byblos.chart.model.Layout;
import dev.byblos.chart.model.VisionType;
import dev.byblos.core.model.StyleExpr;
import dev.byblos.core.stacklang.InvalidSyntaxException;
import dev.byblos.core.util.Features;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.byblos.core.model.TypeUtils.asPresentationType;
import static dev.byblos.core.model.TypeUtils.isPresentationType;
import static java.util.Objects.requireNonNull;

public final class GraphConfigFactory {
    private final DefaultSettings settings;

    public GraphConfigFactory(DefaultSettings settings) {
        this.settings = requireNonNull(settings);
    }

    /**
     * Create a graph config from a request object. This will look at the URI and try to
     * extract some context from the headers.
     */
    public GraphConfig toGraphConfig(GraphRequest request) {
        var q = request.getFirstParam("q");
        if (q.isEmpty()) {
            throw new IllegalArgumentException("missing required parameter 'q'");
        }
        var builder = ImmutableGraphConfig.builder()
                .settings(settings)
                .query(q.get())
                .start(request.getFirstParam("s"))
                .end(request.getFirstParam("e"))
                .step(request.getFirstParam("step"))
                .format(request.getFirstParam("format").orElse("png"))
                .uri(request.uri());

        var id = request.getFirstParam("id").or(() -> {
            // Only look at headers if the id is not explicitly set on the URI
            return request.getFirstHeader("origin").map(GraphConfigFactory::extractHostname);
        }).orElse("default");
        builder.id(id);

        var features = request.getFirstParam("features").map(Features::fromString).orElse(Features.STABLE);
        builder.features(features);

        var axes = IntStream.rangeClosed(0, GraphConstants.MaxYAxis)
                .mapToObj(i -> Map.entry(i, newAxis(request, i)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var vision = request.getFirstParam("vision").map(VisionType::fromString).orElse(VisionType.normal);
        var theme = request.getFirstParam("theme").orElse(settings.theme());
        var palette = request.getFirstParam("palette").orElse(settings.primaryPalette(theme));
        var flags = ImmutableImageFlags.builder()
                .title(request.getFirstParam("title").filter(x -> !x.isEmpty()))
                .width(request.getFirstParam("w").map(Integer::parseInt).orElse(settings.width()))
                .height(request.getFirstParam("h").map(Integer::parseInt).orElse(settings.height()))
                .zoom(request.getFirstParam("zoom").map(Double::parseDouble).orElse(1.0))
                .axes(axes)
                .axisPerLine(asBoolean(request.getFirstParam("axis_per_line")))
                .showLegend(!asBoolean(request.getFirstParam("no_legend")))
                .showLegendStats(!asBoolean(request.getFirstParam("no_legend_stats")))
                .showOnlyGraph(asBoolean(request.getFirstParam("only_graph")))
                .vision(vision)
                .palette(palette)
                .theme(theme)
                .layout(request.getFirstParam("layout").map(Layout::fromString).orElse(Layout.CANVAS))
                .hints(processHints(request.getFirstParam("hints")))
                .build();
        builder.flags(flags);

        var timezones = List.copyOf(Lists.reverse(new ArrayList<>(request.params().get("tz"))));
        builder.timezones(timezones);

        var browser = request.getFirstHeader("user-agent")
                .map(settings::isBrowserAgent)
                .orElse(false);
        builder.browser(browser).allowedFromBrowser(true);

        try {
            builder.parsedQuery(parseQuery(q.get(), timezones, features));
        } catch (Exception e) {
            builder.parseException(e);
        }
        return builder.build();
    }

    private static String extractHostname(String origin) {
        if (origin.startsWith("http:") || origin.startsWith("https:")) {
            try {
                return new URL(origin).getHost();
            } catch (MalformedURLException e) {
                // Do nothing, return origin as a string if not parseable.
            }
        }
        return origin;
    }

    private static Optional<String> getAxisParam(GraphRequest request, String k, int id) {
        return request.getFirstParam(String.format("%s.%s", k, id)).or(() -> request.getFirstParam(k));
    }

    /**
     * Hints parameter is a comma separated set of strings that will be passed on to the
     * rendering engine to optionally tune behavior.
     */
    private static Set<String> processHints(Optional<String> hints) {
        return hints.map(s -> Splitter.on(",")
                .omitEmptyStrings()
                .trimResults()
                .splitToStream(s)
                .collect(Collectors.toSet())
        ).orElse(Set.of());
    }

    private static Axis newAxis(GraphRequest request, int id) {
        // Prefer the scale parameter if present. If not, then fallback to look at
        // the boolean `o` parameter for backwards compatibility.
        var scale = getAxisParam(request, "scale", id).or(() -> {
            return getAxisParam(request, "o", id).filter("1"::equals).map(x -> "log");
        });
        return ImmutableAxis.builder()
                .upper(getAxisParam(request, "u", id))
                .lower(getAxisParam(request, "l", id))
                .scale(scale)
                .stack(asBoolean(getAxisParam(request, "stack", id)))
                .yLabel(getAxisParam(request, "ylabel", id).filter(s -> !s.isEmpty()))
                .tickLabels(getAxisParam(request, "tick_labels", id))
                .palette(request.getFirstParam(String.format("palette.%s", id)))
                .sort(getAxisParam(request, "sort", id))
                .order(getAxisParam(request, "order", id))
                .build();
    }

    private static boolean asBoolean(Optional<String> value) {
        return value.filter("1"::equals).isPresent();
    }

    private List<StyleExpr> parseQuery(String q, List<String> timezones, Features features) throws InvalidSyntaxException {
        var vars = Map.<String, Object>of("tz", GraphConfig.getTimeZoneIds(settings, timezones).get(0));
        var exprs = new ArrayList<StyleExpr>();
        for (var v : settings.interpreter().execute(q, vars, features).stack().reverse()) {
            if (!isPresentationType(v)) {
                throw new IllegalArgumentException(String.format("expecting time series expr, found %s '%s'", v.getClass().getSimpleName(), v));
            }
            exprs.add(asPresentationType(v));
        }
        return exprs;
    }
}
