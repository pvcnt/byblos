package dev.byblos.chart.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface Image {
    Map<String, String> metadata();

    Image withMetadata(Map<String, String> metadata);

    void write(OutputStream output) throws IOException;

    PngImage toPngImage();
}
