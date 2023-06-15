package dev.byblos.core.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

import javax.annotation.Nullable;
import java.awt.Color;
import java.time.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public final class Strings {

    private static final boolean[] allowedInVarName = makeAllowedInVarName();
    // Period following conventions of unix `at` command.
    private static final Pattern AtPeriod = Pattern.compile("^(\\d+)([a-z]+)$");

    // Period following the ISO8601 conventions.
    private static final Pattern IsoPeriod = Pattern.compile("^(P.*)$");

    // Date relative to a given reference point.
    private static final Pattern RelativeDate = Pattern.compile("^([a-z]+)([\\-+])(.+)$");
    // Named date such as `epoch` or `now`.
    private static final Pattern NamedDate = Pattern.compile("^([a-z]+)$");

    // Unix data in seconds since the epoch.
    private static final Pattern UnixDate = Pattern.compile("^([0-9]+)$");

    /**
     * Parse a color expressed as a hexadecimal RRGGBB string.
     */
    public static Color parseColor(String str) {
        var len = str.length();
        checkArgument(len == 3 || len == 6 || len == 8, "color must be hex string [AA]RRGGBB");
        var colorStr = (len == 3) ? str.codePoints().mapToObj(c -> String.valueOf((char) c) + String.valueOf((char) c)).collect(Collectors.joining()) : str;
        if (len <= 6) {
            return new Color(Integer.parseInt(colorStr, 16), false);
        }
        return new Color((int) Long.parseLong(colorStr, 16), true);
    }

    // Standardized date/time constants:
    private final static long oneSecond = 1000L;
    private final static long oneMinute = oneSecond * 60L;
    private final static long oneHour = oneMinute * 60L;
    private final static long oneDay = oneHour * 24L;
    private final static long oneWeek = oneDay * 7L;

    /**
     * Returns a string representation of a period.
     */
    public static String toString(Duration d) {
        var t = d.toMillis();
        if (t % oneWeek == 0) {
            return (t / oneWeek) + "w";
        }
        if (t % oneDay == 0) {
            return (t / oneDay) + "d";
        }
        if (t % oneHour == 0) {
            return (t / oneHour) + "h";
        }
        if (t % oneMinute == 0) {
            return (t / oneMinute) + "m";
        }
        if (t % oneSecond == 0) {
            return (t / oneSecond) + "s";
        }
        return d.toString();
    }

    /**
     * Substitute variables from the map into a string. If a key used in the
     * input string is not set, then the key will be used as the value.
     */
    public static String substitute(String str, Map<String, String> vars) {
        return substitute(str, k -> vars.getOrDefault(k, k));
    }

    /**
     * Substitute variables into a string.
     */
    public static String substitute(String str, Function<String, String> vars) {
        var key = new StringBuilder(str.length());
        var buf = new StringBuilder(str.length() * 2);
        var i = 0;
        while (i < str.length()) {
            var c = str.charAt(i);
            if (c != '$' || i == str.length() - 1) {
                buf.append(c);
                i += 1;
            } else {
                i = getKey(str, i, key);
                var k = key.toString();
                // Empty keys are treated as '$' literals
                if (k.isEmpty()) {
                    buf.append('$');
                } else {
                    buf.append(vars.apply(k));
                }
                key.setLength(0);
            }
        }
        return buf.toString();
    }

    private static int getKey(String str, int i, StringBuilder key) {
        var c = str.charAt(i + 1);
        return (c == '(') ? parenVar(str, i + 2, key) : simpleVar(str, i + 1, key);
    }

    private static int simpleVar(String str, int i, StringBuilder key) {
        var j = i;
        while (j < str.length()) {
            var c = str.charAt(j);
            if (c < allowedInVarName.length && allowedInVarName[c]) {
                key.append(c);
            } else {
                return j;
            }
            j += 1;
        }
        return j;
    }

    private static int parenVar(String str, int i, StringBuilder key) {
        var j = i;
        while (j < str.length()) {
            var c = str.charAt(j);
            if (c != ')') {
                key.append(c);
            } else {
                return j + 1;
            }
            j += 1;
        }
        key.setLength(0);
        return i;
    }

    /**
     * Parse a string that follows the ISO8601 spec or `at` time range spec
     * into a period object.
     */
    public static Duration parseDuration(String str) {
        var matcher = AtPeriod.matcher(str);
        if (matcher.find()) {
            return parseAtDuration(matcher.group(1), matcher.group(2));
        }
        matcher = IsoPeriod.matcher(str);
        if (matcher.find()) {
            return Duration.parse(matcher.group(1));
        }
        throw new IllegalArgumentException("invalid period " + str);
    }

    /**
     * Convert an `at` command time range into a joda period object.
     */
    private static Duration parseAtDuration(String amount, String unit) {
        var v = Long.parseLong(amount);
        if (Set.of("seconds", "second", "s").contains(unit)) {
            return Duration.ofSeconds(v);
        }
        if (Set.of("minutes", "minute", "min", "m").contains(unit)) {
            return Duration.ofMinutes(v);
        }
        if (Set.of("hours", "hour", "h").contains(unit)) {
            return Duration.ofHours(v);
        }
        if (Set.of("days", "day", "d").contains(unit)) {
            return Duration.ofDays(v);
        }
        if (Set.of("weeks", "week", "wk", "w").contains(unit)) {
            return Duration.ofDays(v * 7);
        }
        if (Set.of("months", "month").contains(unit)) {
            return Duration.ofDays(v * 30);
        }
        if (Set.of("years", "year", "y").contains(unit)) {
            return Duration.ofDays(v * 365);
        }
        throw new IllegalArgumentException("unknown unit " + unit);
    }

    /**
     * Returns whether if a date string is relative.
     */
    public static boolean isRelativeDate(String str) {
        return isRelativeDate(str, false);
    }

    /**
     * Returns whether if a date string is relative. If custom ref is true it will
     * check if it is a relative date against a custom reference point other than
     * now or the epoch.
     */
    public static boolean isRelativeDate(String str, boolean customRef) {
        var matcher = RelativeDate.matcher(str);
        if (matcher.find()) {
            return !customRef || !Set.of("now", "epoch").contains(matcher.group(1));
        }
        return false;
    }

    /**
     * Return the time associated with a given string. The time will be relative
     * to `now`.
     */
    public static ZonedDateTime parseDate(String str) {
        return parseDate(str, ZoneOffset.UTC);
    }

    public static ZonedDateTime parseDate(String str, ZoneId tz) {
        return parseDate(ZonedDateTime.now(tz), str, tz);
    }

    /**
     * Return the time associated with a given string.
     * <p>
     * - now, n:
     * - start, s:
     * - end, e:
     * - epoch:
     * <p>
     * - seconds, s:
     * - minutes, m:
     * - hours, h:
     * - days, d:
     * - weeks, w:
     * - months
     * - years, y:
     */
    public static ZonedDateTime parseDate(ZonedDateTime ref, String str, ZoneId tz) {
        var matcher = RelativeDate.matcher(str);
        if (matcher.find()) {
            var op = matcher.group(2);
            if ("-".equals(op)) {
                return parseRefVar(ref, matcher.group(1)).minus(parseDuration(matcher.group(3)));
            }
            if ("+".equals(op)) {
                return parseRefVar(ref, matcher.group(1)).plus(parseDuration(matcher.group(3)));
            }
            throw new IllegalArgumentException("invalid operation " + op);
        }
        matcher = NamedDate.matcher(str);
        if (matcher.find()) {
            return parseRefVar(ref, matcher.group(1));
        }
        matcher = UnixDate.matcher(str);
        if (matcher.find()) {
            // If the value is too big assume it is a milliseconds unit like java uses. The overlap is
            // fairly small and not in the range we typically use:
            // scala> Instant.ofEpochMilli(Integer.MAX_VALUE)
            // res1: java.time.Instant = 1970-01-25T20:31:23.647Z
            var v = Long.parseLong(matcher.group(1));
            var t = (v > Integer.MAX_VALUE) ? v : v * 1000L;
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), tz);
        }
        try {
            return IsoDateTimeParser.parse(str, tz);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid date " + str, e);
        }
    }

    /**
     * Returns the datetime object associated with a given reference point.
     */
    private static ZonedDateTime parseRefVar(ZonedDateTime ref, String v) {
        if ("now".equals(v)) {
            return ZonedDateTime.now(ZoneOffset.UTC);
        }
        if ("epoch".equals(v)) {
            return ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
        }
        return ref;
    }

    /**
     * Parse start and end time strings that can be relative to each other and resolve to
     * precise instants.
     *
     * @param s  Start time string in a format supported by `parseDate`.
     * @param e  End time string in a format supported by `parseDate`.
     * @param tz Time zone to assume for the times if a zone is not explicitly specified. Defaults
     *           to UTC.
     * @return Tuple `start -> end`.
     */
    public static Pair<Instant, Instant> timeRange(String s, String e, ZoneId tz) {
        Pair<Instant, Instant> range;
        if (isRelativeDate(s, true) || "e".equals(s)) {
            checkArgument(!isRelativeDate(e, true), "start and end are both relative");
            var end = Strings.parseDate(e, tz);
            var start = Strings.parseDate(end, s, tz);
            range = new Pair<>(start.toInstant(), end.toInstant());
        } else {
            var start = parseDate(s, tz);
            var end = parseDate(start, e, tz);
            range = new Pair<>(start.toInstant(), end.toInstant());
        }
        checkArgument(isBeforeOrEqual(range.first(), range.second()), "end time is before start time");
        return range;
    }

    public static Pair<Instant, Instant> timeRange(String s, String e) {
        return timeRange(s, e, ZoneOffset.UTC);
    }

    /**
     * Hex decode an input string.
     *
     * @param input      Input string to decode.
     * @param escapeChar Character used to indicate the start of a hex encoded symbol.
     * @return Decoded string.
     */
    public static String hexDecode(String input, char escapeChar) {
        var buf = new StringBuilder();
        var size = input.length();
        var pos = 0;
        while (pos < size) {
            var c = input.charAt(pos);
            if (c == escapeChar) {
                if (size - pos <= 2) {
                    // Not enough room left for two hex characters, copy the rest of
                    // the string to the buffer and end the loop.
                    buf.append(input.substring(pos));
                    pos = size;
                } else {
                    var c1 = hexValue(input.charAt(pos + 1));
                    var c2 = hexValue(input.charAt(pos + 2));
                    if (c1 >= 0 && c2 >= 0) {
                        // Both are hex chars, add decoded character to buffer
                        var nc = (char) (c1 << 4 | c2);
                        buf.append(nc);
                        pos += 2;
                    } else {
                        // Not a valid hex encoding, just echo the escape character
                        // back into the buffer and move on.
                        buf.append(c);
                    }
                }
            } else {
                buf.append(c);
            }
            pos += 1;
        }
        return buf.toString();
    }

    /**
     * Lenient url-decoder. The URLDecoder class provided in the jdk throws
     * if there is an invalid hex encoded value. This function will map invalid
     * encodes to a %25 (a literal percent sign) and then decode it normally.
     */
    public static String urlDecode(String s) {
        return hexDecode(s);
    }

    public static String hexDecode(String input) {
        return hexDecode(input, '%');
    }

    /**
     * Converts a hex character into an integer value. Returns -1 if the input is not a
     * hex character.
     */
    private static int hexValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        return -1;
    }

    private static boolean isBeforeOrEqual(Instant s, Instant e) {
        return s.isBefore(e) || s.equals(e);
    }

    private static final Pattern QueryParam = Pattern.compile("^([^=]+)=(.*)$");

    /**
     * Returns a map corresponding to the URL query parameters in the string.
     */
    public static Multimap<String, String> parseQueryString(@Nullable String query) {
        var params = ImmutableMultimap.<String, String>builder();
        if (null != query) {
            for (var part : query.split("[&;]+")) {
                part = part.replace("+", " ");
                var matcher = QueryParam.matcher(part);
                if (matcher.find()) {
                    params.put(urlDecode(matcher.group(1)), urlDecode(matcher.group(2)));
                } else {
                    params.put(urlDecode(part), "1");
                }
            }
        }
        return params.build();
    }

    private static boolean[] makeAllowedInVarName() {
        var alphabet = new boolean[128];
        alphabet['.'] = true;
        alphabet['-'] = true;
        alphabet['_'] = true;
        for (var c = 'a'; c <= 'z'; c++) {
            alphabet[c] = true;
        }
        for (var c = 'A'; c <= 'Z'; c++) {
            alphabet[c] = true;
        }
        for (var c = '0'; c <= '9'; c++) {
            alphabet[c] = true;
        }
        return alphabet;
    }

    private Strings() {
        // Do not instantiate.
    }
}
