package dev.byblos.chart;

import com.fasterxml.jackson.core.JsonGenerator;
import dev.byblos.chart.model.GraphDef;
import dev.byblos.chart.model.LineDef;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class StdJsonGraphEngine extends JsonGraphEngine {

    public StdJsonGraphEngine() {
        super("json");
    }

    @Override
    protected void write(GraphDef config, JsonGenerator gen) throws IOException {
        var lines = config.plots().stream().flatMap(p -> p.lines().stream()).collect(Collectors.toList());

        gen.writeStartObject();

        writeContext(config, gen);
        writeLegend(lines, gen);
        writeMetrics(lines, gen);
        writeValues(config, lines, gen);
        writeWarnings(config, gen);
        writeCollectorStats(config, gen);

        gen.writeEndObject();
    }

    private void writeValues(GraphDef config, List<LineDef> lines, JsonGenerator gen) throws IOException {
        gen.writeArrayFieldStart("values");
        var step = (int) config.step();
        var endTime = config.endTime().toEpochMilli();
        var timestamp = config.startTime().toEpochMilli();
        while (timestamp < endTime) {
            gen.writeStartArray();
            for (var line : lines) {
                var v = line.data().data().get(timestamp);
                gen.writeNumber(v);
            }
            gen.writeEndArray();
            timestamp += step;
        }
        gen.writeEndArray();
    }

    private void writeCollectorStats(GraphDef config, JsonGenerator gen) throws IOException {
        var start = config.startTime().toEpochMilli() / 1000;
        var end = config.endTime().toEpochMilli() / 1000;
        var graphLines = config.plots().stream().mapToInt(p -> p.data().size()).sum();
        var graphDatapoints = graphLines * ((end - start) / (config.step() / 1000) + 1);

        gen.writeObjectFieldStart("explain");
        if (config.fetchTime().isPresent()) {
            gen.writeNumberField("fetchTime", config.fetchTime().get().toMillis());
        }
        gen.writeNumberField("graphLines", graphLines);
        gen.writeNumberField("graphDatapoints", graphDatapoints);
        gen.writeEndObject();
    }
}
