package dev.byblos.chart;

import com.google.common.annotations.VisibleForTesting;
import dev.byblos.chart.graphics.*;
import dev.byblos.chart.model.GraphDef;
import dev.byblos.chart.util.Fonts;
import dev.byblos.chart.util.Image;
import dev.byblos.chart.util.Throwables;
import dev.byblos.core.util.Strings;
import dev.byblos.core.util.UnitPrefix;

import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.OutputStream;
import java.text.AttributedString;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base class for all graph engines producing an image.
 */
abstract class ImageGraphEngine implements GraphEngine {
    private static final Color USER_ERROR_YELLOW = new Color(0xFF, 0xCF, 0x00);
    private static final Color SYSTEM_ERROR_RED = new Color(0xF8, 0x20, 0x00);

    @Override
    public final boolean shouldOutputImage() {
        return true;
    }

    @Override
    public final void writeGraph(GraphDef config, OutputStream output) throws IOException {
        createImage(config).write(output);
    }

    @Override
    public final void writeError(Throwable t, Dimensions dims, OutputStream output) throws IOException {
        var image = createErrorImage(t, dims);
        image.write(output);
    }

    protected record RenderedGraph(
            List<Element> elements, Color bgColor, Dimensions dims, Map<String, String> metadata) {

        public void draw(Graphics2D g) {
            g.setColor(bgColor);
            g.fillRect(0, 0, dims.width(), dims.height());

            var y = 0;
            for (var element : elements) {
                var h = element.getHeight(g, dims.width());
                element.draw(g, 0, y, dims.width(), y + h);
                y += h;
            }
        }
    }

    protected record RenderedError(String text, Color textColor, Color bgColor, Dimensions dims) {
        public void draw(Graphics2D g) {
            // Try to avoid problems with different default fonts on various platforms. Java will use the
            // "Dialog" font by default which can get mapped differently on various systems. It looks like
            // passing a bad font name into the font constructor will just silently fall back to the
            // default so it should still function if this font isn't present. Uses a default font that
            // is included as part of this library.
            var font = Fonts.DEFAULT;
            g.setFont(font);

            g.setPaint(bgColor);
            g.fill(new Rectangle(0, 0, dims.width(), dims.height()));

            g.setPaint(textColor);
            var attrStr = new AttributedString(text);
            attrStr.addAttribute(TextAttribute.FONT, font);
            var iterator = attrStr.getIterator();
            var measurer = new LineBreakMeasurer(iterator, g.getFontRenderContext());

            var wrap = dims.width() - 8.0f;
            var y = 0.0f;
            while (measurer.getPosition() < text.length()) {
                var layout = measurer.nextLayout(wrap);
                y += layout.getAscent();
                layout.draw(g, 4.0f, y);
                y += layout.getDescent() + layout.getLeading();
            }
        }
    }

    @VisibleForTesting
    final Image createImage(GraphDef config) {
        return createImage(renderGraph(config.computeStats()));
    }

    @VisibleForTesting
    final Image createErrorImage(Throwable t, Dimensions dims) {
        return createErrorImage(renderError(t, dims));
    }

    protected abstract Image createImage(RenderedGraph graph);

    protected abstract Image createErrorImage(RenderedError error);

    private RenderedGraph renderGraph(GraphDef config) {
        var notices = new ArrayList<String>();
        notices.addAll(config.warnings());
        if (config.height() > GraphConstants.MaxHeight) {
            notices.add(String.format("Restricted graph height to %s.", GraphConstants.MaxHeight));
        }
        if (config.width() > GraphConstants.MaxWidth) {
            notices.add(String.format("Restricted graph width to %s.", GraphConstants.MaxWidth));
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
                for (var plot : config.plots()) {
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

            config.fetchTime().ifPresent(fetchTime -> {
                var stats = String.format("Fetch: %sms", fetchTime.toMillis());
                belowCanvas.add(Text.left(stats, ChartSettings.smallFont, config.theme().legend().text()));
            });
        }

        if (!notices.isEmpty() && config.showText()) {
            var warnings = new ArrayList<Element>(1 + 2 * notices.size());
            warnings.add(Text.left("Warnings", ChartSettings.normalFont.deriveFont(Font.BOLD), config.theme().warnings().text()));
            for (var notice : notices) {
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
        var dims = new Dimensions(imgWidth, imgHeight);

        return new RenderedGraph(elements, bgColor, dims, getMetadata(config));
    }

    private String format(double v) {
        return UnitPrefix.decimal(v).format(v);
    }

    private int height(List<Element> elements, int w) {
        return elements.stream().mapToInt(e -> e.getHeight(ChartSettings.refGraphics, w)).sum();
    }

    private RenderedError renderError(Throwable t, Dimensions dims) {
        if (Throwables.isUserError(t)) {
            var text = "USER ERROR: " + t.getMessage();
            return new RenderedError(text, Color.BLACK, USER_ERROR_YELLOW, dims);
        }
        return new RenderedError("SYSTEM ERROR", Color.WHITE, SYSTEM_ERROR_RED, dims);
    }

    private static Map<String, String> getMetadata(GraphDef config) {
        return config.source().map(s -> {
            var desc = String.format("start=%s, end=%s", config.startTime(), config.endTime());
            return Map.of("Source", s, "Description", desc);
        }).orElse(Map.of());
    }
}
