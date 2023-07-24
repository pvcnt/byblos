package dev.byblos.chart;

import dev.byblos.chart.graphics.Dimensions;
import dev.byblos.chart.model.GraphDef;
import dev.byblos.chart.model.LineDef;
import dev.byblos.chart.util.Throwables;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Base class for all graph engines producing JSON.
 */
abstract class JsonGraphEngine implements GraphEngine {
    private final String name;
    protected final static JsonFactory jsonFactory = new JsonFactory();

    JsonGraphEngine(String name) {
        this.name = name;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final String contentType() {
        return "application/json";
    }

    @Override
    public final boolean shouldOutputImage() {
        return false;
    }

    @Override
    public final void writeGraph(GraphDef config, OutputStream output) throws IOException {
        var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        var gen = jsonFactory.createGenerator(writer);
        write(config, gen);
        gen.flush();
    }

    @Override
    public void writeError(Throwable t, Dimensions dims, OutputStream output) throws IOException {
        var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        var gen = jsonFactory.createGenerator(writer);
        gen.writeStartObject();
        gen.writeStringField("code", Throwables.isUserError(t) ? "USER_ERROR" : "SYSTEM_ERROR");
        gen.writeStringField("message", Throwables.getHumanReadableErrorMessage(t));
        gen.writeEndObject();
        gen.flush();
    }

    protected abstract void write(GraphDef config, JsonGenerator gen) throws IOException;

    protected final void writeContext(GraphDef config, JsonGenerator gen) throws IOException {
        gen.writeNumberField("start", config.startTime().toEpochMilli());
        gen.writeNumberField("end", config.endTime().toEpochMilli());
        gen.writeNumberField("step", config.step());
    }

    protected final void writeLegend(List<LineDef> lines, JsonGenerator gen) throws IOException {
        gen.writeArrayFieldStart("legend");
        for (var line : lines) {
            gen.writeString(line.data().label());
        }
        gen.writeEndArray();
    }

    protected final void writeMetrics(List<LineDef> lines, JsonGenerator gen) throws IOException {
        gen.writeArrayFieldStart("metrics");
        for (var line : lines) {
            gen.writeStartObject();
            var tags = line.data().tags().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
            for (var tag : tags) {
                gen.writeStringField(tag.getKey(), tag.getValue());
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    protected final void writeWarnings(GraphDef config, JsonGenerator gen) throws IOException {
        gen.writeArrayFieldStart("notices");
        for (var warning : config.warnings()) {
            gen.writeString(warning);
        }
        gen.writeEndArray();
    }
}
