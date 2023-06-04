package dev.byblos.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link IsoDateTimeParser}.
 */
public class IsoDateTimeParserTest {
    private final List<String> offsetZones = List.of("07", "0700", "070000", "07:00", "07:00:00");

    @Test
    void parseDate() {
        var t = "2020-07-28";
        var expected = ZonedDateTime.parse("2020-07-28T00:00:00Z");
        assertEquals(expected, IsoDateTimeParser.parse(t, ZoneOffset.UTC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Z", "+00", "+0000", "+000000", "+00:00", "+00:00:00"})
    void parseDateWithUTCZone(String zone) {
        var expected = ZonedDateTime.parse("2020-07-28T00:00:00Z");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28" + zone, ZoneOffset.UTC));
    }

    @Test
    void parseDateWithOffsetZone() {
        for (var zone : offsetZones) {
            var expected = ZonedDateTime.parse("2020-07-28T00:00:00+07:00:00");
            assertEquals(expected, IsoDateTimeParser.parse("2020-07-28+" + zone, ZoneOffset.UTC));

            expected = ZonedDateTime.parse("2020-07-28T00:00:00-07:00:00");
            assertEquals(expected, IsoDateTimeParser.parse("2020-07-28-" + zone, ZoneOffset.UTC));
        }
    }

    @Test
    void parseDateTime() {
        var expected = ZonedDateTime.parse("2020-07-28T21:07:00Z");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07", ZoneOffset.UTC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Z", "+00", "+0000", "+000000", "+00:00", "+00:00:00"})
    void parseDateTimeWithUTCZone(String zone) {
        var expected = ZonedDateTime.parse("2020-07-28T21:07:00Z");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07" + zone, ZoneOffset.UTC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"07", "0700", "070000", "07:00", "07:00:00"})
    void parseDateTimeWithOffsetZone(String zone) {
        var expected = ZonedDateTime.parse("2020-07-28T21:07:00+07:00:00");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07+" + zone, ZoneOffset.UTC));

        expected = ZonedDateTime.parse("2020-07-28T21:07:00-07:00:00");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07-" + zone, ZoneOffset.UTC));
    }

    @Test
    void parseDateTimeWithSeconds() {
        var expected = ZonedDateTime.parse("2020-07-28T21:07:56Z");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07:56", ZoneOffset.UTC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Z", "+00", "+0000", "+000000", "+00:00", "+00:00:00"})
    void parseDateTimeWithSecondsAndUTCZone(String zone) {
        var expected = ZonedDateTime.parse("2020-07-28T21:07:56Z");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07:56" + zone, ZoneOffset.UTC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"07", "0700", "070000", "07:00", "07:00:00"})
    void parseDateTimeWithSecondsAndOffsetZone(String zone) {
        var expected = ZonedDateTime.parse("2020-07-28T21:07:56+07:00:00");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07:56+" + zone, ZoneOffset.UTC));

        expected = ZonedDateTime.parse("2020-07-28T21:07:56-07:00:00");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07:56-" + zone, ZoneOffset.UTC));
    }

    @Test
    void parseDateTimeWithMilliseconds() {
        var expected = ZonedDateTime.parse("2020-07-28T21:07:56.195Z");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07:56.195", ZoneOffset.UTC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Z", "+00", "+0000", "+000000", "+00:00", "+00:00:00"})
    void parseDateTimeWithMillisecondsAndUTCZone(String zone) {
        var expected = ZonedDateTime.parse("2020-07-28T21:07:56Z");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07:56" + zone, ZoneOffset.UTC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"07", "0700", "070000", "07:00", "07:00:00"})
    void parseDateTimeWithMillisecondsAndOffsetZone(String zone) {
        var expected = ZonedDateTime.parse("2020-07-28T21:07:56.195+07:00:00");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07:56.195+" + zone, ZoneOffset.UTC));

        expected = ZonedDateTime.parse("2020-07-28T21:07:56.195-07:00:00");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28T21:07:56.195-" + zone, ZoneOffset.UTC));
    }


    @Test
    void parseDateWithOddZone() {
        var expected = ZonedDateTime.parse("2020-07-28T00:00:00+12:34:56");
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28+12:34:56", ZoneOffset.UTC));
    }

    @Test
    void parseDateWithDefaultZone() {
        var zone = ZoneId.of("US/Pacific");
        var expected = ZonedDateTime.parse("2020-07-28T00:00:00", DateTimeFormatter.ISO_DATE_TIME.withZone(zone));
        assertEquals(expected, IsoDateTimeParser.parse("2020-07-28", zone));
    }
}
