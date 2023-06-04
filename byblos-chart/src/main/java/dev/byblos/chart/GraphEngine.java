package dev.byblos.chart;

import dev.byblos.chart.model.GraphDef;

import java.io.IOException;
import java.io.OutputStream;

public interface GraphEngine {
    String name();

    String contentType();

    void write(GraphDef config, OutputStream output) throws IOException;
}
