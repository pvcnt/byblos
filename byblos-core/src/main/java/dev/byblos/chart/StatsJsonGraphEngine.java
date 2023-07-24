package dev.byblos.chart;

import dev.byblos.chart.model.GraphDef;
import dev.byblos.chart.model.LineDef;
import com.fasterxml.jackson.core.JsonGenerator;
import dev.byblos.model.SummaryStats;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns a handful of summary stats instead of all the raw data for a given graph.
 */
public final class StatsJsonGraphEngine extends JsonGraphEngine {

    public StatsJsonGraphEngine() {
        super("stats.json");
    }

    @Override
    protected void write(GraphDef config, JsonGenerator gen) throws IOException {
        var lines = config.plots().stream().flatMap(p -> p.lines().stream()).collect(Collectors.toList());

        gen.writeStartObject();

        writeContext(config, gen);
        writeLegend(lines, gen);
        writeMetrics(lines, gen);
        writeSummaryStats(config, lines, gen);
        writeWarnings(config, gen);

        gen.writeEndObject();
    }

    private void writeSummaryStats(GraphDef config, List<LineDef> lines, JsonGenerator gen) throws IOException {
        gen.writeArrayFieldStart("stats");
        for (var line : lines) {
            var stats = SummaryStats.fromData(line.data(), config.startTime().toEpochMilli(), config.endTime().toEpochMilli());
            gen.writeStartObject();
            gen.writeNumberField("count", stats.count());
            if (stats.count() > 0) {
                gen.writeNumberField("avg", stats.avg());
                gen.writeNumberField("total", stats.total());
                gen.writeNumberField("max", stats.max());
                gen.writeNumberField("min", stats.min());
                gen.writeNumberField("last", stats.last());
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }
}
