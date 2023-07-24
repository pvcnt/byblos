package dev.byblos.eval.graph;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import dev.byblos.chart.Colors;
import dev.byblos.chart.graphics.Dimensions;
import dev.byblos.chart.model.*;
import dev.byblos.chart.util.Throwables;
import dev.byblos.eval.backend.Backend;
import dev.byblos.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class Grapher {
    private final DefaultSettings settings;
    private final Backend backend;
    private static final Logger LOGGER = LoggerFactory.getLogger(Grapher.class);

    public Grapher(DefaultSettings settings, Backend backend) {
        this.settings = requireNonNull(settings);
        this.backend = requireNonNull(backend);
    }

    /**
     * Evaluate the expressions and render a chart using the specified config.
     */
    public GraphResult evalAndRender(GraphConfig config) {
        if (config.parseException().isPresent()) {
            return createErrorResult(config, config.parseException().get());
        }
        try {
            var dataExprs = config.parsedQuery()
                    .stream()
                    .flatMap(e -> e.expr().dataExprs().stream())
                    .distinct()
                    .toList();
            var result = ImmutableListMultimap.<DataExpr, TimeSeries>builder();
            var stopWatch = Stopwatch.createStarted();
            for (var expr : dataExprs) {
                result.putAll(expr, backend.query(config.evalContext(), expr));
            }
            stopWatch.stop();
            return evalAndRender(config, stopWatch.elapsed(), result.build());
        } catch (Exception e) {
            return createErrorResult(config, e);
        }
    }

    private GraphResult createErrorResult(GraphConfig config, Throwable t) {
        var userError = Throwables.isUserError(t);
        if (!userError) {
            LOGGER.error("Error while rendering graph", t);
        }
        var data = new byte[0];
        try {
            data = createErrorImage(t, config);
        } catch (IOException e) {
            LOGGER.error("Error while rendering error", e);
        }
        return userError ? GraphResult.userError(config, data) : GraphResult.systemError(config, data);
    }

    private byte[] createErrorImage(Throwable t, GraphConfig config) throws IOException {
        var dims = new Dimensions(config.flags().width(), config.flags().height());
        var baos = new ByteArrayOutputStream();
        config.engine().writeError(t, dims, baos);
        return baos.toByteArray();
    }

    private GraphResult evalAndRender(GraphConfig config, Duration fetchTime, Multimap<DataExpr, TimeSeries> data) throws IOException {
        var graphDef = create(config, fetchTime, e -> e.expr().eval(config.evalContext(), data));
        if (graphDef.numLines() == 0) {
            // Do not draw an empty graph. "q" is a required parameter.
            throw new IllegalStateException("expression generated no lines");
        }
        var baos = new ByteArrayOutputStream();
        config.engine().writeGraph(graphDef, baos);
        return GraphResult.ok(config, baos.toByteArray());
    }

    /**
     * Create a new graph definition based on the specified config and data.
     */
    private GraphDef create(GraphConfig config, Duration fetchTime, Function<StyleExpr, ResultSet> eval) {
        var warnings = ImmutableList.<String>builder();

        var plotExprs = config.parsedQuery().stream().collect(Collectors.groupingBy(e -> e.axis().orElse(0)));
        var multiY = plotExprs.size() > 1 && !GraphDef.ambiguousMultiY(config.flags().hints());

        var graphPalette = newPalette(config.flags().palette());

        var plots = plotExprs.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(e -> {
                    var axis = config.flags().axes().get(e.getKey());
                    return createPlot(config, eval, axis, e.getValue(), graphPalette, multiY, warnings);
                })
                .collect(Collectors.toList());
        return config.newGraphDef(plots, fetchTime, warnings.build());
    }

    private PlotDef createPlot(GraphConfig config, Function<StyleExpr, ResultSet> eval, Axis axis, List<StyleExpr> exprs, Function<String, Color> graphPalette, boolean multiY, ImmutableList.Builder<String> warnings) {
        var axisPalette = axis.palette().map(Grapher::newPalette).orElse(graphPalette);
        List<String> messages = List.of();
        List<LineDef> lines = new ArrayList<>();
        for (var expr : exprs) {
            var result = eval.apply(expr);

            // Pick the last non-empty message to appear. Right now they are only used
            // as a test for providing more information about the state of filtering. These
            // can quickly get complicated when used with other features. For example,
            // sorting can mix and match lines across multiple expressions. Also binary
            // math operations that combine the results of multiple filter expressions or
            // multi-level group by with filtered input. For now this is just an
            // experiment for the common simple case to see how it impacts usability
            // when dealing with filter expressions that remove some of the lines.
            if (!result.messages().isEmpty()) {
                messages = List.of(result.messages().get(0));
            }
            lines.addAll(createLine(config, result, axis, expr, axisPalette, warnings));
        }

        // Apply sort based on URL parameters. This will take precedence over
        // local sort on an expression.
        var sortedLines = sort(warnings, axis.sort(), axis.useDescending(), lines);

        return axis.newPlotDef(Stream.concat(sortedLines.stream(), messages.stream().map(s -> new MessageDef("... " + s + " ..."))).collect(Collectors.toList()), multiY);
    }

    private List<LineDef> createLine(final GraphConfig config, final ResultSet result, final Axis axis, final StyleExpr expr, final Function<String, Color> axisPalette, final ImmutableList.Builder<String> warnings) {
        var labelledTS = result.data().stream().map(t -> {
            var stats = SummaryStats.fromData(t.data(), config.startMillis(), config.endMillis());
            // Additional stats can be used for substitutions, but should not be included
            // as part of the output tag map
            var legendTags = new HashMap<>(t.tags());
            legendTags.putAll(stats.tags(axis.statFormatter()));
            return Map.entry(new TimeSeries(t.data(), expr.legend(t.label(), legendTags), t.tags()), stats);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var palette = expr.palette().map(Grapher::newPalette).orElseGet(() -> {
            return expr.color().map(c -> {
                var color = settings.resolveColor(config.flags().theme(), c);
                var it = Palette.singleColor(color).iterator();
                return (Function<String, Color>) s1 -> it.next();
            }).orElse(axisPalette);
        });

        var lineDefs = labelledTS.entrySet()
                .stream()
                .sorted(Comparator.comparing(x -> x.getKey().label()))
                .map(x -> createLineDef(config, axis, expr, x.getKey(), x.getValue(), palette))
                .collect(Collectors.toList());

        // Lines must be sorted for presentation after the colors have been applied
        // using the palette. The colors selected should be stable regardless of the
        // sort order that is applied. Otherwise colors would change each time a user
        // changed the sort.
        var sorted = sort(warnings, expr.sortBy(), expr.useDescending(), lineDefs);
        return expr.limit().map(n -> sorted.subList(0, Math.min(sorted.size(), n))).orElse(sorted);
    }

    private LineDef createLineDef(GraphConfig config, Axis axis, StyleExpr expr, TimeSeries ts, SummaryStats stats, Function<String, Color> palette) {
        var lineStyle = expr.lineStyle().map(s -> LineStyle.valueOf(s.toUpperCase())).orElse(axis.lineStyle());
        var color = expr.color()
                .map(c -> settings.resolveColor(config.flags().theme(), c))
                .orElseGet(() -> {
                    Color c = palette.apply(ts.label());
                    // Alpha setting if present will set the alpha value for the color automatically
                    // assigned by the palette. If using an explicit color it will have no effect as the
                    // alpha can be set directly using an ARGB hex format for the color.
                    return expr.alpha().map(a -> Colors.withAlpha(c, a)).orElse(c);
                });

        return ImmutableLineDef.builder()
                .data(ts)
                .query(expr.expr().toString())
                .color(color)
                .lineStyle(lineStyle)
                .lineWidth(expr.lineWidth())
                .legendStats(stats)
                .build();
    }

    /**
     * Creates a new palette and optionally changes it to use the label hash for
     * selecting the color rather than choosing the next available color in the
     * palette. Hash selection is useful to ensure that the same color is always
     * used for a given label even on separate graphs. However, it also means
     * that collisions are more likely and that the same color may be used for
     * different labels even with a small number of lines.
     * <p>
     * Hash mode will be used if the palette name is prefixed with "hash:".
     */
    private static Function<String, Color> newPalette(String mode) {
        var prefix = "hash:";
        if (mode.startsWith(prefix)) {
            var pname = mode.substring(prefix.length());
            var p = Palette.fromString(pname);
            return v -> p.colors(v.hashCode());
        }
        var it = Palette.fromString(mode).iterator();
        return v -> it.next();
    }

    private static List<LineDef> sort(
            ImmutableList.Builder<String> warnings,
            Optional<String> sortBy,
            boolean useDescending,
            List<LineDef> lines
    ) {

        // The default is sort by legend in ascending order. If the defaults have been explicitly
        // changed, then the explicit values should be used. Since the sort by param is used to
        // short circuit if there is nothing to do, it will get set to legend explicitly here if
        // the order has been changed to descending.
        var by = useDescending ? Optional.of(sortBy.orElse("legend")) : sortBy;

        return by.map(mode -> {
            var cmp = getComparator(mode, useDescending, warnings);
            return lines.stream().sorted(cmp).collect(Collectors.toList());
        }).orElse(lines);
    }

    private static Comparator<LineDef> getComparator(String mode, boolean useDescending, ImmutableList.Builder<String> warnings) {
        switch (mode) {
            case "legend" -> {
                return getComparator(x -> x.data().label(), useDescending);
            }
            case "min" -> {
                return getComparator(x -> x.legendStats().min(), useDescending);
            }
            case "max" -> {
                return getComparator(x -> x.legendStats().max(), useDescending);
            }
            case "avg" -> {
                return getComparator(x -> x.legendStats().avg(), useDescending);
            }
            case "count" -> {
                return getComparator(x -> x.legendStats().count(), useDescending);
            }
            case "total" -> {
                return getComparator(x -> x.legendStats().total(), useDescending);
            }
            case "last" -> {
                return getComparator(x -> x.legendStats().last(), useDescending);
            }
            default -> {
                warnings.add(String.format("Invalid sort mode '%s'. Using default of 'legend'.", mode));
                return getComparator(x -> x.data().label(), useDescending);
            }
        }
    }

    private static <T extends Comparable<T>> Comparator<LineDef> getComparator(Function<LineDef, T> keyExtractor, boolean useDescending) {
        return Comparator.comparing(keyExtractor, useDescending ? Comparator.reverseOrder() : Comparator.naturalOrder());
    }
}
