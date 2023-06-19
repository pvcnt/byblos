package dev.byblos.chart;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Functions;
import com.google.common.io.Resources;
import dev.byblos.chart.model.*;
import dev.byblos.chart.test.GraphAssertions;
import dev.byblos.chart.test.SrcPath;
import dev.byblos.core.model.ArrayTimeSeq;
import dev.byblos.core.model.FunctionTimeSeq;
import dev.byblos.core.model.TimeSeries;
import org.junit.jupiter.api.*;

import java.awt.*;
import java.io.IOException;
import java.time.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

abstract class ImageGraphEngineTest {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    private final String dataDir = "graphengine/data";
    private static GraphAssertions graphAssertions;

    private static final boolean bless = false;
    private static final int step = 60000;

    abstract public String getPrefix();

    abstract public ImageGraphEngine graphEngine();

    @BeforeAll
    static void beforeEach(TestInfo testInfo) {
        var testClass = testInfo.getTestClass().get();
        var baseDir = SrcPath.forProject("byblos-chart");
        var goldenDir = baseDir + "/src/test/resources/graphengine/" + testClass.getSimpleName();
        var targetDir = baseDir + "/target/" + testClass.getSimpleName();
        graphAssertions = new GraphAssertions(goldenDir, targetDir, Assertions::assertEquals);
    }

    @AfterAll
    static void afterAll(TestInfo testInfo) throws IOException {
        graphAssertions.generateReport(testInfo.getTestClass().get());
    }

    @Test
    void nonUniformlyDrawnSpikes() throws Exception {
        var name = getPrefix() + "_non_uniformly_drawn_spikes." + graphEngine().name();
        var dataFileName = getPrefix() + "_non_uniformly_drawn_spikes.json";

        var graphDef = load(dataDir + "/" + dataFileName).toBuilder().width(700).build();
        // Byblos generated sample is 780 wide less 64 origin less 16 r side padding == 700
        // expect to see width of spikes vary as x values repeat due to rounding
        // RrdGraph calculates x values based on number of pixels/second
        check(name, graphDef);
    }

    @Test
    void notices() throws Exception {
        var plotDef = ImmutablePlotDef.builder().data(label(simpleSeriesDef(400))).build();

        var graphDef = ImmutableGraphDef.builder()
                .startTime(ZonedDateTime.of(2012, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant())
                .endTime(ZonedDateTime.of(2012, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC).toInstant())
                .addPlots(plotDef)
                .themeName("light")
                .addWarnings("This is an information message that is shown on the graph to let the user know about something important. It should be long enough to force the message to wrap.")
                .addWarnings("Something bad happened and we wanted you to know.")
                .addWarnings("Something really bad happened.")
                .build();

        var name = getPrefix() + "_notices." + graphEngine().name();
        check(name, graphDef);
    }

    @Test
    void simpleSingleLine() throws Exception {
        singleLine("single_line", v -> v);
    }

    @Test
    void singleLineWithFetchTime() throws Exception {
        singleLine("single_line_with_fetch_time", v -> v.toBuilder().fetchTime(Duration.ofMillis(5123)).build());
    }

    @Test
    void singleLineOnlyGraph() throws Exception {
        singleLine("single_line_only_graph", v -> v.toBuilder().onlyGraph(true).build());
    }

    @Test
    void singleLineTitle() throws Exception {
        singleLine("single_line_title", v -> v.toBuilder().title("A sample title").build());
    }

    @Test
    void singleLineNoLegend() throws Exception {
        singleLine("single_line_no_legend", v -> v.toBuilder().legendType(LegendType.OFF).build());
    }

    @Test
    void singleLineNoLegendStats() throws Exception {
        singleLine("single_line_no_legend_stats", v -> v.toBuilder().legendType(LegendType.LABELS_ONLY).build());
    }

    @Test
    void singleLineLineWidth() throws Exception {
        singleLine("single_line_linewidth", v -> v.adjustLines(x -> x.toBuilder().lineWidth(3.0f).build()));
    }

    @Test
    void singleLineUpper() throws Exception {
        singleLine("single_line_upper", v -> v.adjustPlots(x -> x.toBuilder().upper(new ExplicitBound(200)).build()));
    }

    @Test
    void singleLineLower() throws Exception {
        singleLine("single_line_lower", v -> v.adjustPlots(x -> x.toBuilder().lower(new ExplicitBound(200)).build()));
    }

    @Test
    void singleLineYLabel() throws Exception {
        singleLine("single_line_ylabel", v -> v.adjustPlots(x -> x.toBuilder().yLabel("something useful").build()));
    }

    @Test
    void singleLineArea() throws Exception {
        singleLine("single_line_area", v -> v.adjustLines(x -> x.toBuilder().lineStyle(LineStyle.AREA).build()));
    }

    @Test
    void singleLineStack() throws Exception {
        singleLine("single_line_stack", v -> v.adjustLines(x -> x.toBuilder().lineStyle(LineStyle.STACK).build()));
    }

    @Test
    void singleLineColor() throws Exception {
        singleLine("single_line_color", v -> v.adjustLines(x -> x.toBuilder().color(Color.BLUE).build()));
    }

    @Test
    void singleLineLogarithmic() throws Exception {
        singleLine("single_line_logarithmic", v -> v.adjustPlots(x -> x.toBuilder().scale(Scale.LOGARITHMIC).build()));
    }

    @Test
    void singleLinePower() throws Exception {
        singleLine("single_line_power", v -> v.adjustPlots(x -> x.toBuilder().scale(Scale.POWER_2).build()));
    }

    @Test
    void singleLineSqrt() throws Exception {
        singleLine("single_line_sqrt", v -> v.adjustPlots(x -> x.toBuilder().scale(Scale.SQRT).build()));
    }

    @Test
    void singleLineNoTickLabels() throws Exception {
        singleLine("single_line_no_tick_labels", v -> v.adjustPlots(x -> x.toBuilder().tickLabelMode(TickLabelMode.OFF).build()));
    }

    @Test
    void singleLineLayoutImage() throws Exception {
        singleLine("single_line_layout_image", v -> v.toBuilder().layout(Layout.IMAGE).build());
    }

    @Test
    void singleLineLayoutImageHeight() throws Exception {
        singleLine("single_line_layout_ih", v -> v.toBuilder().layout(Layout.IMAGE_HEIGHT).build());
    }

    @Test
    void singleLineLayoutImageWidth() throws Exception {
        singleLine("single_line_layout_iw", v -> v.toBuilder().layout(Layout.IMAGE_WIDTH).build());
    }

    @Test
    void singleLineLayoutImageWidth50() throws Exception {
        singleLine("single_line_layout_iw_50", v -> v.toBuilder().layout(Layout.IMAGE_WIDTH).width(50).build());
    }

    @Test
    void singleLineLayoutImageWidth100() throws Exception {
        singleLine("single_line_layout_iw_100", v -> v.toBuilder().layout(Layout.IMAGE_WIDTH).width(100).build());
    }

    @Test
    void singleLineLayoutImageWidth1000() throws Exception {
        singleLine("single_line_layout_iw_1000", v -> v.toBuilder().layout(Layout.IMAGE_WIDTH).width(1000).build());
    }

    @Test
    void singleLineLayoutImageWidth10000() throws Exception {
        singleLine("single_line_layout_iw_10000", v -> v.toBuilder().layout(Layout.IMAGE_WIDTH).width(10_000).build());
    }

    @Test
    void singleLineTimezone() throws Exception {
        singleLine("single_line_timezone",
                v -> v.toBuilder().timezones(List.of(ZoneId.of("US/Pacific"))).build());
    }

    @Test
    void singleLineTimezonesAB() throws Exception {
        var timezones = List.of(ZoneId.of("US/Pacific"), ZoneId.of("UTC"));
        singleLine("single_line_timezones_ab", v -> v.toBuilder().timezones(timezones).build());
    }

    @Test
    void singleLineTimezonesBA() throws Exception {
        var timezones = List.of(ZoneId.of("UTC"), ZoneId.of("US/Pacific"));
        singleLine("single_line_timezones_ba", v -> v.toBuilder().timezones(timezones).build());
    }

    @Test
    void singleLineTimezonesMany() throws Exception {
        var timezones = List.of(
                ZoneId.of("US/Pacific"),
                ZoneId.of("UTC"),
                ZoneId.of("Europe/Berlin"),
                ZoneId.of("Australia/Eucla"));
        singleLine("single_line_timezones_many", v -> v.toBuilder().timezones(timezones).build());
    }

    @Test
    void singleLineLongLabel() throws Exception {
        var longLabel = "A long ylabel that should cause it to wrap when displayed on the chart. Some more text to" +
                " ensure that it will wrap when showing in the legend.";
        singleLine("single_line_ylabel_wrap", v -> v.adjustPlots(x -> x.toBuilder().yLabel(longLabel).build()));
    }

    @Test
    void singleLineLogNegative() throws Exception {
        lines("single_line_log_negative",
                List.of(-400d),
                v -> v.adjustPlots(x -> x.toBuilder().scale(Scale.LOGARITHMIC).build()));
    }

    @Test
    void singleLineLogLarge() throws Exception {
        lines("single_line_log_large",
                List.of(4.123e9),
                v -> v.adjustPlots(x -> x.toBuilder().scale(Scale.LOGARITHMIC).build()));
    }

    @Test
    void singleLinePowerNegative() throws Exception {
        lines("single_line_power_negative",
                List.of(-400d),
                v -> v.adjustPlots(x -> x.toBuilder().scale(Scale.POWER_2).build()));
    }

    @Test
    void singleLinePowerLarge() throws Exception {
        lines("single_line_power_large",
                List.of(4.123e9),
                v -> v.adjustPlots(x -> x.toBuilder().scale(Scale.POWER_2).build()));
    }

    @Test
    void singleLineSqrtNegative() throws Exception {
        lines("single_line_sqrt_negative",
                List.of(-400d),
                v -> v.adjustPlots(x -> x.toBuilder().scale(Scale.SQRT).build()));
    }

    @Test
    void singleLineSqrtLarge() throws Exception {
        lines("single_line_sqrt_large",
                List.of(4.123e9),
                v -> v.adjustPlots(x -> x.toBuilder().scale(Scale.SQRT).build()));
    }

    @Test
    void singleLineStackNegative() throws Exception {
        lines("single_line_stack_negative",
                List.of(-400d),
                v -> v.adjustLines(x -> x.toBuilder().lineStyle(LineStyle.STACK).build()));
    }

    @Test
    void singleLine50() throws Exception {
        lines("single_line_50", IntStream.range(1, 51).mapToObj(v -> (double) v).toList(), Functions.identity());
    }

    @Test
    void constantLineLowerBound0() throws Exception {
        constantLine("lower_bound_0", List.of(0d), v -> v.adjustPlots(x -> x.toBuilder().lower(new ExplicitBound(0)).build()));
    }

    @Test
    void constantLineLowerBound4() throws Exception {
        constantLine("lower_bound_4", List.of(4d), v -> v.adjustPlots(x -> x.toBuilder().lower(new ExplicitBound(4)).build()));
    }

    @Test
    void constantLineStack() throws Exception {
        constantLine("stack", List.of(0d), v -> v.adjustLines(x -> x.toBuilder().lineStyle(LineStyle.STACK).build()));
    }

    @Test
    void constantLineArea() throws Exception {
        constantLine("area", List.of(0d), v -> v.adjustLines(x -> x.toBuilder().lineStyle(LineStyle.AREA).build()));
    }

    @Test
    void constantLineStackAuto() throws Exception {
        constantLine("stack_auto", List.of(200d, 100d), v -> v
                .adjustPlots(x -> x.toBuilder().lower(AutoDataBound.INSTANCE).build())
                .adjustLines(x -> x.toBuilder().lineStyle(LineStyle.STACK).build()));
    }

    @Test
    void constantLineL1U2h300() throws Exception {
        constantLine("l1_u2_h300", List.of(1d), v -> v.toBuilder().height(300).build());
    }

    @Test
    void constantLinePositiveInfinity() throws Exception {
        constantLine("positive_infinity", List.of(Double.POSITIVE_INFINITY), Functions.identity());
    }

    @Test
    void constantLineNegativeInfinity() throws Exception {
        constantLine("negative_infinity", List.of(Double.NEGATIVE_INFINITY), Functions.identity());
    }

    @Test
    void constantLineDoubleMax() throws Exception {
        constantLine("double_max", List.of(Double.MAX_VALUE), Functions.identity());
    }

    @Test
    void constantLineDoubleMin() throws Exception {
        constantLine("double_min", List.of(-Double.MAX_VALUE), Functions.identity());
    }

    @Test
    void constantLineDoubleMinPositive() throws Exception {
        constantLine("double_min_positive", List.of(Double.MIN_VALUE), Functions.identity());
    }

    @Test
    void constantLineDoubleMinZero() throws Exception {
        constantLine("double_min_zero", List.of(Double.MIN_VALUE, 0d), Functions.identity());
    }

    @Test
    void constantLineDoubleLarge() throws Exception {
        constantLine("double_large", List.of(1.234e28, 7.85e23), Functions.identity());
    }

    @Test
    void constantLineDoubleSmall() throws Exception {
        constantLine("double_small", List.of(1.234e-28, 7.85e-23), Functions.identity());
    }

    private static List<LineDef> label(LineDef... vs) {
        return label(0, Palette.DEFAULT, vs);
    }

    private static List<LineDef> label(List<LineDef> vs) {
        return label(vs.toArray(new LineDef[0]));
    }

    private static List<LineDef> label(int offset, Palette p, LineDef... vs) {
        return IntStream.range(0, vs.length).mapToObj(i -> {
            var color = p.withAlpha(vs[i].color().getAlpha()).colors(i + offset);
            var timeSeries = vs[i].data().withLabel(String.valueOf(i));
            return vs[i].toBuilder().data(timeSeries).color(color).build();
        }).collect(Collectors.toList());
    }


    private static TimeSeries wave(double min, double max, Duration wavelength) {
        var lambda = 2 * Math.PI / wavelength.toMillis();
        var f = new Function<Long, Double>() {
            @Override
            public Double apply(Long t) {
                var amp = (max - min) / 2.0;
                var yoffset = min + amp;
                return amp * Math.sin(t * lambda) + yoffset;
            }
        };
        var tags = Map.of("name", "wave");
        return new TimeSeries(new FunctionTimeSeq(step, f), TimeSeries.defaultLabel(tags), tags);
    }

    private static TimeSeries simpleWave(double min, double max) {
        return wave(min, max, Duration.ofDays(1));
    }

    private static TimeSeries simpleWave(double max) {
        return simpleWave(0, max);
    }

    private static TimeSeries constant(double v) {
        var tags = Map.of("name", String.valueOf(v));
        return new TimeSeries(new FunctionTimeSeq(step, l -> v), TimeSeries.defaultLabel(tags), tags);
    }

    private static LineDef simpleSeriesDef(double min, double max) {
        return ImmutableLineDef.builder()
                .data(simpleWave(min, max))
                .query(min + "," + max)
                .build();
    }

    private static LineDef simpleSeriesDef(double max) {
        return simpleSeriesDef(0, max);
    }

    private LineDef constantSeriesDef(double value) {
        return ImmutableLineDef.builder().data(constant(value)).build();
    }

    private void lines(String name, List<Double> vs, Function<GraphDef, GraphDef> f) throws IOException {
        var series = vs.stream().map(v -> Double.isNaN(v) ? constantSeriesDef(v) : simpleSeriesDef(v)).collect(Collectors.toList());
        var plotDef = ImmutablePlotDef.builder().data(label(series)).build();

        var graphDef = ImmutableGraphDef.builder()
                .startTime(ZonedDateTime.of(2012, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant())
                .endTime(ZonedDateTime.of(2012, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC).toInstant())
                .addPlots(plotDef)
                .themeName("light")
                .build();

        var fname = getPrefix() + "_" + name + "." + graphEngine().name();
        check(fname, f.apply(graphDef));
    }

    private void singleLine(String name, Function<GraphDef, GraphDef> f) throws IOException {
        lines(name, List.of(400d), f);
    }

    private void constantLine(String name, List<Double> vs, Function<GraphDef, GraphDef> f) throws IOException {
        var testName = "constant_line_" + name;
        var series = vs.stream().map(this::constantSeriesDef).toList();
        var plotDef = ImmutablePlotDef.builder().data(label(series)).build();

        var graphDef = ImmutableGraphDef.builder()
                .startTime(ZonedDateTime.of(2012, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant())
                .endTime(ZonedDateTime.of(2012, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC).toInstant())
                .addPlots(plotDef)
                .themeName("light")
                .build();

        var fname = getPrefix() + "_" + testName + "." + graphEngine().name();
        check(fname, f.apply(graphDef));
    }

    private static GraphDef load(String resource) throws Exception {
        var bytes = Resources.toByteArray(Resources.getResource(resource));
        return GraphData.decode(bytes).toGraphDef();
    }

    private void check(String name, GraphDef graphDef) throws IOException {
        AssertionError caught = null;
        try {
            checkImpl(name, graphDef);
        } catch (AssertionError e) {
            caught = e;
        }
        try {
            checkImpl("dark_" + name, graphDef.toBuilder().themeName("dark").build());
        } catch (AssertionError e) {
            caught = e;
        }
        if (null != caught) {
            throw caught;
        }
    }

    private void checkImpl(String name, GraphDef graphDef) throws IOException {
        // var json = JsonCodec.encode(graphDef);
        // assertEquals(graphDef.normalize, JsonCodec.decode(json).normalize);

        var image = graphEngine().createImage(graphDef).withMetadata(Map.of());
        graphAssertions.assertEquals(image, name, bless);
    }

    private final static class GraphData {
        @JsonProperty("start")
        long start;

        @JsonProperty("step")
        long step;

        @JsonProperty("legend")
        List<String> legend = List.of();

        @JsonProperty("metrics")
        List<Map<String, String>> metrics = List.of();

        @JsonProperty("values")
        List<List<Double>> values = List.of();

        public static GraphData decode(byte[] bytes) throws IOException {
            return objectMapper.readValue(bytes, GraphData.class);
        }

        GraphDef toGraphDef() {
            var nbrSteps = values.size() - 1;
            var s = Instant.ofEpochMilli(start);
            var e = s.plusMillis(step * nbrSteps);

            var seq = new ArrayTimeSeq(s.toEpochMilli(), step, values.stream().flatMap(Collection::stream).mapToDouble(v -> v).toArray());
            var seriesDef = ImmutableLineDef.builder().data(new TimeSeries(seq, "0", Map.of())).build();
            var plotDef = ImmutablePlotDef.builder().data(List.of(seriesDef)).build();

            return ImmutableGraphDef.builder()
                    .startTime(s)
                    .endTime(e)
                    .addPlots(plotDef)
                    .step(step)
                    .themeName("light")
                    .build();
        }
    }

}
