package dev.byblos.eval.graph;

import com.google.common.collect.Multimap;

import java.util.Optional;

public record GraphRequest(String uri, Multimap<String, String> params, Multimap<String, String> headers) {
    public Optional<String> getFirstParam(String key) {
        return getFirstValue(params(), key);
    }

    public Optional<String> getFirstHeader(String key) {
        return getFirstValue(headers(), key);
    }

    private static Optional<String> getFirstValue(Multimap<String, String> map, String key) {
        var values = map.get(key);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.iterator().next());
    }
}
