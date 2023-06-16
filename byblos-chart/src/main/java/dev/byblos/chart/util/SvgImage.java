package dev.byblos.chart.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public record SvgImage(String document, Map<String, String> metadata) implements Image {
    @Override
    public SvgImage withMetadata(Map<String, String> metadata) {
        return new SvgImage(document, metadata);
    }

    @Override
    public PngImage toPngImage() {
        return null;
    }

    @Override
    public void write(OutputStream output) throws IOException {
        var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        writer.append(document);
        writer.flush();
    }
}
