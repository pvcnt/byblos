package dev.byblos.chart;

import dev.byblos.chart.model.GraphDef;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

abstract class CsvGraphEngine implements GraphEngine {
    private final String name;
    private final String contentType;
    private final String sep;

    CsvGraphEngine(String name, String contentType, String sep) {
        this.name = name;
        this.contentType = contentType;
        this.sep = sep;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final String contentType() {
        return contentType;
    }

    @Override
    public final void write(GraphDef config, OutputStream output) throws IOException {
        var lines = config.plots().stream().flatMap(p -> p.lines().stream()).toList();
        var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        writer.append("\"timestamp\"");
        for (var line : lines) {
            var label = String.format("\"%s\"", line.data().label());
            writer.append(sep).append(label);
        }
        writer.append("\n");
        var step = config.step();
        var endTime = config.endTime().toEpochMilli();
        var timestamp = config.startTime().toEpochMilli();
        while (timestamp < endTime) {
            var t = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), config.timezone());
            writer.append(t.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            for (var line : lines) {
                var v = line.data().data().get(timestamp);
                var vstr = String.format(config.numberFormat(), v);
                writer.append(sep).append(vstr);
            }
            writer.append("\n");
            timestamp += step;
        }
        writer.flush();
    }
}
