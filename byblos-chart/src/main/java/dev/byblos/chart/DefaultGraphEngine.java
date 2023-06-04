package dev.byblos.chart;

import com.netflix.iep.config.ConfigManager;
import dev.byblos.chart.graphics.*;
import dev.byblos.chart.model.GraphDef;
import dev.byblos.core.util.Strings;
import dev.byblos.core.util.UnitPrefix;

import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DefaultGraphEngine extends PngGraphEngine {
    private static final Map<RenderingHints.Key, Object> renderingHints = makeRenderingHints();

    @Override
    public String name() {
        return "png";
    }

    @Override
    protected RenderedImage createImage(GraphDef gdef) {
        var config = gdef.computeStats();

        var notices = new ArrayList<String>();
        notices.addAll(config.warnings());
        if (config.height() > GraphConstants.MaxHeight) {
            notices.add(String.format("Restricted graph height to %s.", GraphConstants.MaxHeight));
        }
        if (config.width() > GraphConstants.MaxWidth) {
            notices.add(String.format("Restricted graph width to %s.", GraphConstants.MaxWidth));
        }
        if (config.zoom() > GraphConstants.MaxZoom) {
            notices.add(String.format("Restricted zoom to %s.", GraphConstants.MaxZoom));
        }

        final var aboveCanvas = new ArrayList<Element>();
        config.title().ifPresent(str -> {
            if (config.showText()) {
                aboveCanvas.add(Text.center(str, ChartSettings.largeFont, config.theme().image().text()).truncate(config.width()));
            }
        });
        aboveCanvas.add(new HorizontalPadding(5));

        var hoffset = config.layout().isFixedHeight() ? height(aboveCanvas, config.width()) : 0;
        var graph = new TimeSeriesGraph(config.toBuilder().height(config.height() - hoffset).build());

        final var belowCanvas = new ArrayList<Element>();
        if (config.showLegend()) {
            var entriesPerPlot = (config.numLines() <= GraphConstants.MaxLinesInLegend)
                ? GraphConstants.MaxLinesInLegend
                    : GraphConstants.MaxLinesInLegend / config.plots().size();
            var showStats = config.showLegendStats() && graph.width() >= ChartSettings.minWidthForStats;
            belowCanvas.add(new HorizontalPadding(5));
            if (config.plots().size() > 1) {
                for (var i = 0; i < config.plots().size(); i++) {
                    final var j = i;
                    var plot = config.plots().get(i);
                    var label = plot.yLabel().map(s -> String.format("Axis %s: %s", j, s)).orElseGet(() -> String.format("Axis %s", j));
                    belowCanvas.add(new Legend(config.theme().legend(), plot, label, showStats, entriesPerPlot));
                }
            } else {
                for (var plot: config.plots()) {
                    belowCanvas.add(new Legend(config.theme().legend(), plot, null, showStats, entriesPerPlot));
                }
            }

            var start = config.startTime().toEpochMilli();
            var end = config.endTime().toEpochMilli();
            var frame = Strings.toString(Duration.between(config.startTime(), config.endTime()));
            var endTime = ZonedDateTime.ofInstant(config.endTime(), config.timezone()).toString();
            var step = Strings.toString(Duration.ofMillis(config.step()));
            var comment = String.format("Frame: %s, End: %s, Step: %s", frame, endTime, step);
            belowCanvas.add(new HorizontalPadding(15));
            belowCanvas.add(Text.left(comment, ChartSettings.smallFont, config.theme().legend().text()));

            if (config.loadTime() > 0 && config.stats().inputLines() > 0) {
                var graphLines = config.plots().stream().mapToInt(p -> p.data().size()).sum();
                var graphDatapoints = graphLines * ((end - start) / (config.step() / 1000) + 1);
                var stats = String.format(
                        "Fetch: %sms (L: %s, %s, %s; D: %s, %s, %s)",
                        config.loadTime(),
                        format(config.stats().inputLines()),
                        format(config.stats().outputLines()),
                        format(graphLines),
                        format(config.stats().inputDatapoints()),
                        format(config.stats().outputDatapoints()),
                        format(graphDatapoints)
                );
                belowCanvas.add(Text.left(stats, ChartSettings.smallFont, config.theme().legend().text()));
            } else if (config.loadTime() > 0) {
                var stats = String.format("Fetch: %sms", config.loadTime());
                belowCanvas.add(Text.left(stats, ChartSettings.smallFont, config.theme().legend().text()));
            }
        }

        if (!notices.isEmpty() && config.showText()) {
            var warnings = new ArrayList<Element>(1 + 2 * notices.size());
            warnings.add(Text.left("Warnings", ChartSettings.normalFont.deriveFont(Font.BOLD), config.theme().warnings().text()));
            for (var notice: notices) {
                warnings.add(new HorizontalPadding(2));
                warnings.add(new ListItem(Text.left(notice, config.theme().warnings().text())));
            }
            belowCanvas.add(new HorizontalPadding(15));
            belowCanvas.add(new Block(warnings, config.theme().warnings().background().color()));
        }

        var bgColor = (!notices.isEmpty() && (!config.showText() || config.layout().isFixedHeight()))
            ? config.theme().warnings().background().color()
            : config.theme().image().background().color();

        var elements = new ArrayList<Element>(aboveCanvas.size() + 1 + belowCanvas.size());
        elements.addAll(aboveCanvas);
        elements.add(graph);
        if (!config.layout().isFixedHeight()) {
            elements.addAll(belowCanvas);
        }

        var imgWidth = graph.width();
        var imgHeight = height(elements, imgWidth);

        var zoom = Math.min(config.zoom(), GraphConstants.MaxZoom);
        var zoomWidth = (int) (imgWidth * zoom);
        var zoomHeight = (int) (imgHeight * zoom);
        var image = new BufferedImage(zoomWidth, zoomHeight, BufferedImage.TYPE_INT_ARGB);
        var g = image.createGraphics();
        renderingHints.forEach(g::setRenderingHint);
        g.scale(zoom, zoom);
        g.setColor(bgColor);
        g.fillRect(0, 0, imgWidth, imgHeight);

        var y = 0;
        for (var element: elements) {
            var h = element.getHeight(ChartSettings.refGraphics, imgWidth);
            element.draw(g, 0, y, imgWidth, y + h);
            y += h;
        }

        return image;
    }

    private String format(double v) {
        return UnitPrefix.decimal(v).format(v);
    }

    private int height(List<Element> elements, int w) {
        return elements.stream().mapToInt(e -> e.getHeight(ChartSettings.refGraphics, w)).sum();
    }

    private static Map<RenderingHints.Key, Object> makeRenderingHints() {
        var config = ConfigManager.load().getConfig("byblos.chart.rendering-hints");
        return config.entrySet().stream().map(entry -> {
            var k = (RenderingHints.Key) getField(entry.getKey());
            var v = getField((String) entry.getValue().unwrapped());
            return Map.entry(k, v);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Object getField(String name) {
        try {
            return RenderingHints.class.getField(name).get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
