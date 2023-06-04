package dev.byblos.webapi;


import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;

import java.util.Set;

public final class ApiSettings {
    private final Set<String> excludedWords;

    public ApiSettings(Config config) {
        this.excludedWords = ImmutableSet.copyOf(config.getStringList("expr.complete.excluded-words"));
    }

    public static ApiSettings fromConfig(Config root) {
        return new ApiSettings(root.getConfig("byblos.webapi"));
    }

    public Set<String> excludedWords() {
        return excludedWords;
    }
}
