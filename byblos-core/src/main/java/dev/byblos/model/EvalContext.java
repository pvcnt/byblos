package dev.byblos.model;

import static com.google.common.base.Preconditions.checkArgument;

public final class EvalContext {
    private final long start;
    private final long end;
    private final long step;

    public EvalContext(long start, long end, long step) {
        checkArgument(start < end, String.format("start time must be less than end time (%s >= %s)", start, end));
        this.start = start;
        this.end = end;
        this.step = step;
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }

    public long step() {
        return step;
    }

    public EvalContext withOffset(long offset) {
        var dur = offset / step * step;
        return (dur < step) ? this : new EvalContext(start - dur, end - dur, step);
    }
}
