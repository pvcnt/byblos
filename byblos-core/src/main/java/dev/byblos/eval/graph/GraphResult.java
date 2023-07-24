package dev.byblos.eval.graph;

public record GraphResult(GraphConfig config, byte[] data, Code code) {
    public enum Code {
        OK, USER_ERROR, SYSTEM_ERROR
    }

    public static GraphResult ok(GraphConfig config, byte[] data) {
        return new GraphResult(config, data, Code.OK);
    }

    public static GraphResult userError(GraphConfig config, byte[] data) {
        return new GraphResult(config, data, Code.USER_ERROR);
    }

    public static GraphResult systemError(GraphConfig config, byte[] data) {
        return new GraphResult(config, data, Code.SYSTEM_ERROR);
    }
}
