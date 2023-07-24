package dev.byblos.util;

import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Strings}.
 */
public class StringsTest {
    @Test
    void parseColor() {
        assertEquals(Strings.parseColor("FF0000"), Color.RED);
        assertEquals(Strings.parseColor("ff0000"), Color.RED);
    }

    @Test
    void parseColorWithTripleHex() {
        assertEquals(Strings.parseColor("f00"), Color.RED);
        assertEquals(Strings.parseColor("F00"), Color.RED);
    }

    @Test
    void parseColorWithAlpha() {
        var c = Strings.parseColor("0FFF0000");
        assertEquals(c.getAlpha(), 15);
        assertEquals(c.getRed(), 255);
        assertEquals(c.getGreen(), 0);
        assertEquals(c.getBlue(), 0);
    }

    @Test
    void isRelativeDate() {
        assertTrue(Strings.isRelativeDate("now-6h"));
        assertTrue(Strings.isRelativeDate("epoch+6h"));
        assertTrue(Strings.isRelativeDate("s-6h"));
        assertTrue(Strings.isRelativeDate("start+6h"));
        assertTrue(Strings.isRelativeDate("e-6h"));
        assertTrue(Strings.isRelativeDate("end+6h"));
    }

    @Test
    void isRelativeDateWithCustomRef() {
        assertFalse(Strings.isRelativeDate("now-6h", true));
        assertFalse(Strings.isRelativeDate("epoch+6h", true));
        assertTrue(Strings.isRelativeDate("s-6h", true));
        assertTrue(Strings.isRelativeDate("start+6h", true));
        assertTrue(Strings.isRelativeDate("e-6h", true));
        assertTrue(Strings.isRelativeDate("end+6h", true));
    }

    @Test
    void parseDurationAtSeconds() {
        assertEquals(Strings.parseDuration("42seconds"), Duration.ofSeconds(42));
        assertEquals(Strings.parseDuration("42second"), Duration.ofSeconds(42));
        assertEquals(Strings.parseDuration("42s"), Duration.ofSeconds(42));
    }

    @Test
    void parseDurationAtMinutes() {
        assertEquals(Strings.parseDuration("42minutes"), Duration.ofMinutes(42));
        assertEquals(Strings.parseDuration("42minute"), Duration.ofMinutes(42));
        assertEquals(Strings.parseDuration("42min"), Duration.ofMinutes(42));
        assertEquals(Strings.parseDuration("42m"), Duration.ofMinutes(42));
    }

    @Test
    void parseDurationAtHours() {
        assertEquals(Strings.parseDuration("42hours"), Duration.ofHours(42));
        assertEquals(Strings.parseDuration("42hour"), Duration.ofHours(42));
        assertEquals(Strings.parseDuration("42h"), Duration.ofHours(42));
    }

    // TODO: Need a combination of period with duration using java.time, similar to joda Period

    @Test
    void parseDurationAtDays() {
        assertEquals(Strings.parseDuration("42days"), Duration.ofDays(42));
        assertEquals(Strings.parseDuration("42day"), Duration.ofDays(42));
        assertEquals(Strings.parseDuration("42d"), Duration.ofDays(42));
    }

    @Test
    void parseDurationAtWeeks() {
        assertEquals(Strings.parseDuration("42weeks"), Duration.ofDays(42 * 7));
        assertEquals(Strings.parseDuration("42week"), Duration.ofDays(42 * 7));
        assertEquals(Strings.parseDuration("42wk"), Duration.ofDays(42 * 7));
        assertEquals(Strings.parseDuration("42w"), Duration.ofDays(42 * 7));
    }

    @Test
    void parseDurationAtMonths() {
        assertEquals(Strings.parseDuration("42months"), Duration.ofDays(42 * 30));
        assertEquals(Strings.parseDuration("42month"), Duration.ofDays(42 * 30));
    }

    @Test
    void parseDurationAtYears() {
        assertEquals(Strings.parseDuration("42years"), Duration.ofDays(42 * 365));
        assertEquals(Strings.parseDuration("42year"), Duration.ofDays(42 * 365));
        assertEquals(Strings.parseDuration("42y"), Duration.ofDays(42 * 365));
    }

    @Test
    void parseDurationAtInvalidUnit() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> Strings.parseDuration("42fubars"));
        assertEquals(t.getMessage(), "unknown unit fubars");
    }

    @Test
    void parseDurationIso() {
        // assertEquals(parseDuration("P42Y"), Period.years(42))
        assertEquals(Strings.parseDuration("PT42M"), Duration.ofMinutes(42));
    }

    @Test
    void parseDateIsoDateOnly() {
        var expected = ZonedDateTime.of(2012, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        var actual = Strings.parseDate("2012-02-01");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateIsoDateWithTimeNSeconds() {
        // Note: will fail prior to 8u20:
        // https://github.com/Netflix/atlas/issues/9
        var expected = ZonedDateTime.of(2012, 2, 1, 4, 5, 0, 0, ZoneOffset.UTC);
        assertEquals(Strings.parseDate("2012-02-01T04:05"), expected);
        assertEquals(Strings.parseDate("2012-02-01T04:05Z"), expected);

        var actual = ZonedDateTime.ofInstant(Strings.parseDate("2012-02-01T12:05+08:00").toInstant(), ZoneOffset.UTC);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateIsoDateWithTime() {
        // Note: will fail prior to 8u20:
        // https://github.com/Netflix/atlas/issues/9
        var expected = ZonedDateTime.of(2012, 2, 1, 4, 5, 6, 0, ZoneOffset.UTC);
        var actual = Strings.parseDate("2012-02-01T04:05:06");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateIsoDateWithTimeAndZone() {
        var expected = ZonedDateTime.of(2012, 1, 31, 20, 5, 6, 0, ZoneOffset.UTC);
        var actual = ZonedDateTime.ofInstant(Strings.parseDate("2012-02-01T04:05:06+08:00").toInstant(), ZoneOffset.UTC);
        assertEquals(actual, expected);
    }

    @Test
    void parseDateIsoDateWithTimeWithMillisAndZoneUTC() {
        var nanos = (int) TimeUnit.MILLISECONDS.toNanos(123);
        var expected = ZonedDateTime.of(2012, 2, 1, 4, 5, 6, nanos, ZoneOffset.UTC);
        var actual = Strings.parseDate("2012-02-01T04:05:06.123Z");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateIsoDateWithTimeWithMillisAndZone() {
        var nanos = (int) TimeUnit.MILLISECONDS.toNanos(123);
        var expected = ZonedDateTime.of(2012, 2, 1, 4, 5, 6, nanos, ZoneOffset.UTC);
        var actual = Strings.parseDate("2012-02-01T04:05:06.123+0000");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateIsoDateWithTimeWithMillisAndZoneWithColon() {
        var nanos = (int) TimeUnit.MILLISECONDS.toNanos(123);
        var expected = ZonedDateTime.of(2012, 2, 1, 4, 5, 6, nanos, ZoneOffset.UTC);
        var actual = Strings.parseDate("2012-02-01T04:05:06.123+00:00");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateIsoDateWithTimeWithMillisAndZoneWithTwoDigits() {
        var nanos = (int) TimeUnit.MILLISECONDS.toNanos(123);
        var expected = ZonedDateTime.of(2012, 2, 1, 4, 5, 6, nanos, ZoneOffset.UTC);
        var actual = Strings.parseDate("2012-02-01T04:05:06.123+00");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateIsoDateWithTimeWithMillisAndZoneOffset() {
        var nanos = (int) TimeUnit.MILLISECONDS.toNanos(123);
        var expected = ZonedDateTime.of(2012, 2, 1, 7, 5, 6, nanos, ZoneOffset.UTC).toInstant();
        var actual = Strings.parseDate("2012-02-01T04:05:06.123-03:00").toInstant();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateIsoFormats() {
        var offsets = List.of(
                "",
                "Z",
                "+00",
                "+0000",
                "+000000",
                "+00:00",
                "+00:00:00",
                "-04",
                "-0402",
                "-040231",
                "-04:02",
                "-04:02:31"
        );
        var times = List.of(
                "2020-07-28",
                "2020-07-28T05:43",
                "2020-07-28T05:43:02",
                "2020-07-28T05:43:02.143"
        );
        for (var time : times) {
            for (var offset : offsets) {
                Strings.parseDate(time + offset);
            }
        }
    }

    @Test
    void parseDateIsoDateWithTimeWithMillis() {
        var nanos = (int) TimeUnit.MILLISECONDS.toNanos(123);
        var expected = ZonedDateTime.of(2012, 2, 1, 4, 5, 6, nanos, ZoneOffset.UTC);
        var actual = Strings.parseDate("2012-02-01T04:05:06.123");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateIsoInvalid() {
        assertThatThrownBy(() -> Strings.parseDate("2012-02-"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseDateRelativeMinus() {
        var ref = ZonedDateTime.of(2012, 2, 1, 3, 0, 0, 0, ZoneOffset.UTC);
        var expected = ZonedDateTime.of(2012, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        var actual = Strings.parseDate(ref, "e-3h", ZoneOffset.UTC);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateRelativePlus() {
        var ref = ZonedDateTime.of(2012, 2, 1, 3, 0, 0, 0, ZoneOffset.UTC);
        var expected = ZonedDateTime.of(2012, 2, 1, 3, 0, 42, 0, ZoneOffset.UTC);
        var actual = Strings.parseDate(ref, "start+42s", ZoneOffset.UTC);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateRelativeIso() {
        var ref = ZonedDateTime.of(2012, 2, 2, 3, 0, 0, 0, ZoneOffset.UTC);
        var expected = ZonedDateTime.of(2012, 2, 1, 2, 54, 18, 0, ZoneOffset.UTC);
        var actual = Strings.parseDate(ref, "start-P1DT5M42S", ZoneOffset.UTC);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateEpochPlus4h() {
        var ref = ZonedDateTime.of(2012, 2, 2, 3, 0, 0, 0, ZoneOffset.UTC);
        var expected = ZonedDateTime.of(1970, 1, 1, 4, 0, 0, 0, ZoneOffset.UTC);
        var actual = Strings.parseDate(ref, "epoch+4h", ZoneOffset.UTC);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateRelativeInvalidOp() {
        assertThatThrownBy(() -> Strings.parseDate("e*42h"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid date e*42h");
    }

    @Test
    void parseDateNamedNow() {
        var expected = System.currentTimeMillis();
        var actual = Strings.parseDate("now").toInstant().toEpochMilli();
        assertThat(actual).isGreaterThanOrEqualTo(expected);
    }

    @Test
    void parseDateNamedEpoch() {
        var actual = Strings.parseDate("epoch").toInstant().toEpochMilli();
        assertThat(actual).isEqualTo(0L);
    }

    @Test
    void parseDateUnix() {
        var ref = ZonedDateTime.of(2012, 2, 1, 3, 0, 0, 0, ZoneOffset.UTC);
        var refStr = String.format("%d", ref.toInstant().toEpochMilli() / 1000);
        var actual = Strings.parseDate(refStr);
        assertThat(actual).isEqualTo(ref);
    }

    @Test
    void parseDateUnixMillis() {
        var ref = ZonedDateTime.of(2012, 2, 1, 3, 0, 0, 0, ZoneOffset.UTC);
        var refStr = String.format("%d", ref.toInstant().toEpochMilli());
        var actual = Strings.parseDate(refStr);
        assertThat(actual).isEqualTo(ref);
    }

    @Test
    void parseDateStartEqEndMinus0h() {
        var ref = ZonedDateTime.of(2012, 2, 1, 3, 0, 0, 0, ZoneOffset.UTC);
        var expected = ZonedDateTime.of(2012, 2, 1, 3, 0, 0, 0, ZoneOffset.UTC);
        var actual = Strings.parseDate(ref, "e-0h", ZoneOffset.UTC);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseDateStartEqEnd() {
        var ref = ZonedDateTime.of(2012, 2, 1, 3, 0, 0, 0, ZoneOffset.UTC);
        var expected = ZonedDateTime.of(2012, 2, 1, 3, 0, 0, 0, ZoneOffset.UTC);
        var actual = Strings.parseDate(ref, "e", ZoneOffset.UTC);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toStringDurationInWeeks() {
        var actual = Strings.toString(Duration.ofDays(5 * 7));
        assertThat(actual).isEqualTo("5w");
    }

    @Test
    void toStringDurationInWeeksPlus10h() {
        // P5WT10H would be preferred, but it doesn't parse with java.time:
        // java> Duration.parse("P5WT10H")
        // java.time.format.DateTimeParseException: Text cannot be parsed to a Duration
        //
        // P35DT10H would be better than 850h, but Duration.toString returns PT850H
        // java> Duration.parse("P35DT10H")
        // res6: java.time.Duration = PT850H
        //
        // If it becomes enough of a pain point we can customize the output for the fallback
        assertEquals(Strings.toString(Duration.ofDays(5 * 7).plusHours(10)), "850h");
    }

    @Test
    void toStringDurationInDays() {
        assertEquals(Strings.toString(Duration.ofDays(5)), "5d");
    }

    @Test
    void toStringDurationInHours() {
        assertEquals(Strings.toString(Duration.ofHours(5)), "5h");
    }

    @Test
    void toStringDurationInMinutes() {
        assertEquals(Strings.toString(Duration.ofMinutes(5)), "5m");
    }

    @Test
    void toStringDurationInSeconds() {
        assertEquals(Strings.toString(Duration.ofSeconds(5)), "5s");
    }

    @Test
    void substitute() {
        var vars = Map.of("foo", "bar", "bar", "baz");
        assertEquals(Strings.substitute("$(foo)", vars), "bar");
        assertEquals(Strings.substitute("$(bar)", vars), "baz");
        assertEquals(Strings.substitute("$foo", vars), "bar");
        assertEquals(Strings.substitute("$(foo)$(bar)", vars), "barbaz");
        assertEquals(Strings.substitute("$(foo) ::: $(bar)", vars), "bar ::: baz");
        assertEquals(Strings.substitute("$(missing) ::: $(bar)", vars), "missing ::: baz");
        assertEquals(Strings.substitute("$missing ::: $bar", vars), "missing ::: baz");
    }

    @Test
    void substituteWhenEndsWithDollar() {
        var vars = Map.of("foo", "bar", "bar", "baz");
        assertEquals(Strings.substitute("foo$", vars), "foo$");
    }

    @Test
    void substituteWhenFollowedByWhitespace() {
        var vars = Map.of("foo", "bar", "bar", "baz");
        assertEquals(Strings.substitute("$ foo", vars), "$ foo");
    }

    @Test
    void substituteWhenWhitespaceInParen() {
        var vars = Map.of(" foo", "bar", "bar", "baz");
        assertEquals(Strings.substitute("$( foo)", vars), "bar");
    }

    @Test
    void substituteWhenParensUsedToEscapeLiteralDollar() {
        var vars = Map.of("foo", "bar", "bar", "baz");
        assertEquals(Strings.substitute("$()foo", vars), "$foo");
    }

    @Test
    void substituteWhenUnmatchedOpenParen() {
        var vars = Map.of("foo", "bar", "bar", "baz");
        assertEquals(Strings.substitute("$(foo", vars), "$foo");
        assertEquals(Strings.substitute("foo$(", vars), "foo$");
    }

    @Test
    void substituteWhenUnmatchedClosedParen() {
        var vars = Map.of("foo", "bar", "bar", "baz");
        assertEquals(Strings.substitute("$)foo", vars), "$)foo");
        assertEquals(Strings.substitute("foo$)", vars), "foo$)");
    }

    @Test
    void hexDecodeEscapeWithUnderscore() {
        var str = "a b %25 _ %_% _21%zb";
        assertEquals(Strings.hexDecode(str, '_'), "a b %25 _ %_% !%zb");
    }

    @Test
    void rangeBothAbsolute() {
        var range = Strings.timeRange("2018-07-24", "2018-07-24T00:05");
        assertEquals(range.first(), Strings.parseDate("2018-07-24").toInstant());
        assertEquals(range.second(), Strings.parseDate("2018-07-24T00:05").toInstant());
    }

    @Test
    void rangeEndIsBeforeStart() {
        assertThrows(IllegalArgumentException.class, () -> Strings.timeRange("2018-07-24T00:05", "2018-07-24"));
    }

    @Test
    void rangeStartTimeIsTheSameAsEndTime() {
        var range = Strings.timeRange("2018-07-24", "2018-07-24");
        assertEquals(range.first(), range.second());
    }

    @Test
    void rangeBothRelative() {
        assertThrows(IllegalArgumentException.class, () -> Strings.timeRange("e-5m", "s+5m"));
    }

    @Test
    void rangeStartRelativeToEnd() {
        var range = Strings.timeRange("e-5m", "2018-07-24T00:05");
        assertEquals(range.first(), Strings.parseDate("2018-07-24").toInstant());
        assertEquals(range.second(), Strings.parseDate("2018-07-24T00:05").toInstant());
    }

    @Test
    void rangeEndRelativeToStart() {
        var range = Strings.timeRange("2018-07-24", "s+5m");
        assertEquals(range.first(), Strings.parseDate("2018-07-24").toInstant());
        assertEquals(range.second(), Strings.parseDate("2018-07-24T00:05").toInstant());
    }

    @Test
    void parseQueryStringWhenNull() {
        assertThat(Strings.parseQueryString(null)).isEmpty();
    }

    @Test
    void parseQueryString() {
        var query = "foo=bar&foo=baz;bar&foo=%21&;foo&abc=42";
        assertThat(Strings.parseQueryString(query)).contains(
                MapEntry.entry("abc", "42"),
                MapEntry.entry("bar", "1"),
                MapEntry.entry("foo", "bar"),
                MapEntry.entry("foo", "baz"),
                MapEntry.entry("foo", "!"),
                MapEntry.entry("foo", "1")
        );
    }

    @Test
    void parseQueryStringWithPlusSign() {
        var query = "foo=foo+bar";
        assertThat(Strings.parseQueryString(query)).contains(MapEntry.entry("foo", "foo bar"));
    }
}
