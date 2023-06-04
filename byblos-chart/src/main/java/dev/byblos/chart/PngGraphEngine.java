package dev.byblos.chart;

import dev.byblos.chart.model.GraphDef;
import dev.byblos.chart.util.PngImage;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

abstract class PngGraphEngine implements GraphEngine {

    public String contentType() {
        return "image/png";
    }

    public void write(GraphDef config, OutputStream output) throws IOException {
        var image = new PngImage(createImage(config), getMetadata(config));
        image.write(output);
  }

  protected abstract RenderedImage createImage(GraphDef config);

    private static Map<String, String> getMetadata(GraphDef config) {
        return config.source().map(s -> {
            var desc = String.format("start=%s, end=%s", config.startTime(), config.endTime());
            return Map.of("Source", s, "Description", desc);
        }).orElse(Map.of());
    }
}
