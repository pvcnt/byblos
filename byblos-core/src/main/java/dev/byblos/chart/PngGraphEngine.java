package dev.byblos.chart;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.iep.config.ConfigManager;
import dev.byblos.chart.util.PngImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A graph engine that produces a PNG image.
 */
public final class PngGraphEngine extends ImageGraphEngine {
    private static final Map<RenderingHints.Key, Object> renderingHints = makeRenderingHints();

    // Should we use antialiasing? This will typically need to be disabled for tests to
    // get reliable image comparisons.
    @VisibleForTesting
    static boolean useAntiAliasing = true;

    @Override
    public String name() {
        return "png";
    }

    @Override
    public String contentType() {
        return "image/png";
    }

    @Override
    protected PngImage createImage(RenderedGraph graph) {
        var image = new BufferedImage(graph.dims().width(), graph.dims().height(), BufferedImage.TYPE_INT_ARGB);
        var g = image.createGraphics();
        renderingHints.forEach(g::setRenderingHint);
        graph.draw(g);
        return new PngImage(image, graph.metadata());
    }

    @Override
    protected PngImage createErrorImage(RenderedError error) {
        var image = new BufferedImage(error.dims().width(), error.dims().height(), BufferedImage.TYPE_INT_ARGB);
        var g = image.createGraphics();
        if (useAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        error.draw(g);
        return new PngImage(image, Map.of());
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
