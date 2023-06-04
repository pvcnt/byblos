package dev.byblos.core.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TimeSeries(TimeSeq data, String label, Map<String, String> tags) {

    public TimeSeries withData(TimeSeq data) {
        return new TimeSeries(data, label, tags);
    }

    public TimeSeries withLabel(String label) {
        return new TimeSeries(data, label, tags);
    }

    public TimeSeries withTags(Map<String, String> tags) {
        return new TimeSeries(data, label, tags);
    }

    public static String defaultLabel(Map<String, String> tags) {
        if (tags.isEmpty()) {
            return "NO TAGS";
        }
        return defaultLabel(tags.keySet().stream().sorted().collect(Collectors.toList()), tags);
    }

    private static String defaultLabel(List<String> keys, Map<String, String> tags) {
        return keys.stream().map(k -> k + "=" + tags.getOrDefault(k, "NULL")).collect(Collectors.joining(", "));
    }
}
