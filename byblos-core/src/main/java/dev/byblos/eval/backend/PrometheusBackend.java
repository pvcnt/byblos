package dev.byblos.eval.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.typesafe.config.Config;
import dev.byblos.model.DataExpr;
import dev.byblos.model.EvalContext;
import dev.byblos.model.FunctionTimeSeq;
import dev.byblos.model.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PrometheusBackend implements Backend {
    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusBackend.class);
    private static final String NAME_TAG = "__name__";

    public PrometheusBackend(Config config) {
        baseUrl = config.getString("endpoint");
        client = createHttpClient();
        objectMapper = createObjectMapper();
        LOGGER.info("Connected to {}", baseUrl);
    }

    @Override
    public List<TimeSeries> query(EvalContext context, DataExpr expr) throws IOException {
        return query(context, expr.exprString()).stream()
                .map(result -> toTimeSeries(context, result))
                .collect(Collectors.toList());
    }

    private List<Result> query(EvalContext context, String query) throws IOException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/query_range?start=" + (context.start() / 1000) + "&end=" + (context.end() / 1000) + "&step=" + (context.step() / 1000) + "&query=" + urlEncode(query)))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .build();
        try {
            var httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            validateResponse(httpResponse);
            var response = objectMapper.readValue(httpResponse.body(), QueryResponse.class);
            return response.data.result;
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while executing a request", e);
            return List.of();
        }
    }

    private static TimeSeries toTimeSeries(EvalContext context, Result result) {
        if (result.values.isEmpty()) {
            throw new RuntimeException("Histograms are not supported");
        }
        var label = result.metric.get(NAME_TAG);
        var tags = new HashMap<>(result.metric);
        tags.remove(NAME_TAG);
        if (!tags.isEmpty()) {
            label += "{" + TimeSeries.defaultLabel(tags) + "}";
        }
        var values = result.values.stream().collect(Collectors.toMap(v -> v.time.toEpochMilli(), v -> v.value));
        var data = new FunctionTimeSeq(context.step(), t -> values.getOrDefault(t, Double.NaN));
        return new TimeSeries(data, label, result.metric);
    }

    private static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    private static void validateResponse(HttpResponse<String> response) throws IOException {
        if (response.statusCode() != 200) {
            throw new IOException(String.format("Unexpected status code [%s]: %s", response.statusCode(), response.body()));
        }
    }

    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    private static ObjectMapper createObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    private static class QueryResponse {
        @JsonProperty
        public RangeVector data;
    }

    private static class RangeVector {
        @JsonProperty
        public List<Result> result = List.of();
    }

    private static class Result {
        @JsonProperty
        public Map<String, String> metric;
        @JsonProperty
        public List<ScalarValue> values;
    }

    @JsonDeserialize(using = ScalarValueDeserializer.class)
    private static class ScalarValue {
        public Instant time;
        public double value;

        public ScalarValue(Instant time, double value) {
            this.time = time;
            this.value = value;
        }
    }

    private static class ScalarValueDeserializer extends StdDeserializer<ScalarValue> {
        public ScalarValueDeserializer(Class<?> vc) {
            super(vc);
        }

        @SuppressWarnings("unused")
        public ScalarValueDeserializer() {
            this(null);
        }

        @Override
        public ScalarValue deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            var time = Instant.ofEpochSecond(node.get(0).longValue());
            var value = Double.parseDouble(node.get(1).textValue());
            return new ScalarValue(time, value);
        }
    }
}
