package dev.byblos.model;

import com.google.common.collect.Multimap;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class TimeExpr implements MathExpr {
    private final String mode;
    private final ChronoField chronoField;
    private final static Map<String, ChronoField> chronoFields = Map.ofEntries(
            Map.entry("secondOfMinute", ChronoField.SECOND_OF_MINUTE),
            Map.entry("secondOfDay", ChronoField.SECOND_OF_DAY),
            Map.entry("minuteOfHour", ChronoField.MINUTE_OF_HOUR),
            Map.entry("minuteOfDay", ChronoField.MINUTE_OF_DAY),
            Map.entry("hourOfDay", ChronoField.HOUR_OF_DAY),
            Map.entry("dayOfWeek", ChronoField.DAY_OF_WEEK),
            Map.entry("dayOfMonth", ChronoField.DAY_OF_MONTH),
            Map.entry("dayOfYear", ChronoField.DAY_OF_YEAR),
            Map.entry("monthOfYear", ChronoField.MONTH_OF_YEAR),
            Map.entry("yearOfCentury", ChronoField.YEAR),
            Map.entry("yearOfEra", ChronoField.YEAR_OF_ERA),
            Map.entry("seconds", ChronoField.INSTANT_SECONDS),
            Map.entry("minutes", ChronoField.INSTANT_SECONDS),
            Map.entry("hours", ChronoField.INSTANT_SECONDS),
            Map.entry("days", ChronoField.INSTANT_SECONDS),
            Map.entry("weeks", ChronoField.INSTANT_SECONDS)
    );

    public TimeExpr(String mode) {
        this.mode = mode;
        chronoField = Optional.ofNullable(chronoFields.get(mode)).orElseGet(() -> ChronoField.valueOf(mode));
    }

    @Override
    public ResultSet eval(EvalContext context, Multimap<DataExpr, TimeSeries> data) {
        var seq = new FunctionTimeSeq(context.step(), valueFunc());
        var ts = MathExpr.newTimeSeries(mode, seq);
        return new ResultSet(this, List.of(ts), List.of());
    }

    @Override
    public String toString() {
        return String.format("%s,:time", mode);
    }

    private Function<Long, Double> valueFunc() {
        if (chronoField != ChronoField.INSTANT_SECONDS) {
            return this::usingCalendar;
        }
        return switch (mode) {
            case "seconds" -> sinceEpoch(1000L);
            case "minutes" -> sinceEpoch(1000L * 60L);
            case "hours" -> sinceEpoch(1000L * 60L * 60L);
            case "days" -> sinceEpoch(1000L * 60L * 60L * 24L);
            case "weeks" -> sinceEpoch(1000L * 60L * 60L * 24L * 7L);
            default -> throw new AssertionError();
        };
    }

    private Function<Long, Double> sinceEpoch(long divisor) {
        return t -> (double) t / divisor;
    }

    private double usingCalendar(long t) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneOffset.UTC).get(chronoField);
    }
}
