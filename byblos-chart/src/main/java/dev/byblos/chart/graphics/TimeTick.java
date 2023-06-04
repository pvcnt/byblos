package dev.byblos.chart.graphics;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tick mark for the time axis.
 *
 * @param timestamp Time in milliseconds since the epoch.
 * @param zone      Time zone to use for the string label associated with the timestamp.
 * @param major     True if the position is a major tick mark.
 * @param formatter Formats the timestamp to a string shown on the axis. If set to None, then a default
 *                  will be chosen to try and land on a significant time boundary.
 */
public final class TimeTick {
    private final long timestamp;
    private final boolean major; // Default: true
    private final String label;

    public TimeTick(long timestamp, ZoneId zone, boolean major) {
        this(timestamp, zone, major, null);
    }

    public TimeTick(long timestamp, ZoneId zone, boolean major, @Nullable DateTimeFormatter formatter) {
        this.timestamp = timestamp;
        this.major = major;

        var datetime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zone);
        var fmt = (null == formatter) ? getDefaultFormatter(datetime) : formatter;
        this.label = fmt.format(datetime);
    }

    public boolean major() {
        return major;
    }

    public long timestamp() {
        return timestamp;
    }

    public String label() {
        return label;
    }

    private static DateTimeFormatter getDefaultFormatter(ZonedDateTime datetime) {
        return Ticks.timeBoundaries.stream()
                .filter(f -> datetime.get(f.first) != 0)
                .map(f -> f.second)
                .findFirst()
                .orElse(Ticks.defaultTimeFmt);
    }
}