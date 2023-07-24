package dev.byblos.chart;

import dev.byblos.chart.util.SvgImage;
import org.jfree.svg.SVGGraphics2D;

import java.util.Map;

/**
 * A graph engine that produces an SVG image.
 */
public final class SvgGraphEngine extends ImageGraphEngine {
    @Override
    public String name() {
        return "svg";
    }

    @Override
    public String contentType() {
        return "image/svg+xml";
    }

    @Override
    protected SvgImage createImage(RenderedGraph graph) {
        var g = new SVGGraphics2D(graph.dims().width(), graph.dims().height());
        // renderingHints.forEach(g::setRenderingHint);
        graph.draw(g);
        return new SvgImage(g.getSVGDocument(), graph.metadata());
    }

    @Override
    protected SvgImage createErrorImage(RenderedError error) {
        var g = new SVGGraphics2D(error.dims().width(), error.dims().height());
        // renderingHints.forEach(g::setRenderingHint);
        error.draw(g);
        return new SvgImage(g.getSVGDocument(), Map.of());
    }
}
