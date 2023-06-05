package dev.byblos.eval.graph;

import java.util.Optional;

public record GraphResult(GraphConfig config, byte[] data, Code code, Optional<String> message) {
    public enum Code {
        OK, USER_ERROR, SYSTEM_ERROR
    }

    public static GraphResult ok(GraphConfig config, byte[] data) {
        return new GraphResult(config, data, Code.OK, Optional.empty());
    }

    public static GraphResult userError(GraphConfig config, byte[] data, String message) {
        return new GraphResult(config, data, Code.USER_ERROR, Optional.of(message));
    }

    public static GraphResult systemError(GraphConfig config, byte[] data, String message) {
        return new GraphResult(config, data, Code.SYSTEM_ERROR, Optional.of(message));
    }
}
