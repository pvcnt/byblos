package dev.byblos.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {ByblosTestApplication.class}
)
public class ExprResourceTest {

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Test
    public void debugNoParams() {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/debug"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void debug() throws Exception {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/debug?q=42,stack,:ls"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = readDebugOutput(response);
        assertThat(data).hasSize(4);
    }

    @Test
    public void debugWithVariables() throws Exception {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/debug?q=foo,bar,:set,foo,:get"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = readDebugOutput(response);
        assertThat(data).hasSize(6);
        assertThat(Iterables.getLast(data).context.stack).containsExactly("bar");
        assertThat(Iterables.getLast(data).context.variables).containsExactly(Map.entry("foo", "bar"));
    }

    @Test
    public void debugWithVocabulary() throws Exception {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/debug?q=42,stack,:ls,up,f00,:color&vocab=style"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = readDebugOutput(response);
        assertThat(data).hasSize(7);
    }

    @Test
    public void debugWithUnknownWordInVocabulary() {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/debug?q=42,stack,:ls&vocab=query"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("{\"message\":\"unknown word ':ls'\"}");
    }

    @Test
    public void debugWithUnknownVocabulary() {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/debug?q=42,stack,:ls&vocab=foo"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("{\"message\":\"unknown vocabulary [foo]\"}");
    }

    @Test
    public void normalizeNoParams() {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/normalize"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void normalize() throws Exception {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/normalize?q=up,stack,:ls"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = readNormalizeOutput(response);
        assertThat(data).containsExactly("up,stack,:ls");
    }

    @Test
    public void normalizeMultipleItems() throws Exception {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/normalize?q=up,stack,:ls,down"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = readNormalizeOutput(response);
        assertThat(data).containsExactly("up,stack,:ls", "down");
    }

    @Test
    public void normalizeConstAndQuery() throws Exception {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/normalize?q=42,:const,up,:query"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = readNormalizeOutput(response);
        assertThat(data).containsExactly("42.0", "up");
    }

    @Test
    public void normalizeVarsInLegend() throws Exception {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/normalize?q=up,$foo,:legend"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var data = readNormalizeOutput(response);
        assertThat(data).containsExactly("up,$(foo),:legend");
    }

    @Test
    public void evalNoParams() {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/eval"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void completeNoParams() {
        var response = restTemplate.getForEntity(urlFor("/api/v1/expr/complete"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String urlFor(String path) {
        return "http://localhost:" + port + path;
    }

    private List<Step> readDebugOutput(ResponseEntity<String> response) throws JsonProcessingException {
        return objectMapper.readValue(response.getBody(), new TypeReference<List<Step>>() {
        });
    }

    private List<String> readNormalizeOutput(ResponseEntity<String> response) throws JsonProcessingException {
        return objectMapper.readValue(response.getBody(), new TypeReference<List<String>>() {
        });
    }

    private List<Expr> readEvalOutput(ResponseEntity<String> response) throws JsonProcessingException {
        return objectMapper.readValue(response.getBody(), new TypeReference<List<Expr>>() {
        });
    }

    private List<Candidate> readCompleteOutput(ResponseEntity<String> response) throws JsonProcessingException {
        return objectMapper.readValue(response.getBody(), new TypeReference<List<Candidate>>() {
        });
    }

    record Step(List<String> program, Context context) {
    }

    record Context(List<String> stack, Map<String, String> variables) {
    }

    record Candidate(String name, String signature, String description) {
    }

    record Expr(String expr, long offset, Map<String, String> settings) {
    }
}
