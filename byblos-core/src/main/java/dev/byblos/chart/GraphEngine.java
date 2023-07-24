package dev.byblos.chart;

import dev.byblos.chart.graphics.Dimensions;
import dev.byblos.chart.model.GraphDef;

import java.io.IOException;
import java.io.OutputStream;

public interface GraphEngine {
    String name();

    String contentType();

    boolean shouldOutputImage();

    void writeGraph(GraphDef config, OutputStream output) throws IOException;

    void writeError(Throwable t, Dimensions dims, OutputStream output) throws IOException;
}
