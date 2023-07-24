package dev.byblos.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper for parsing the variations of ISO date/time formats that are used with Byblos. Since
 * the DateTimeFormatter doesn't have a way to check if a string matches, this class uses
 * pattern matching to normalize to a small number of cases (with and without zone) and avoid
 * using exceptions as the control flow.
 */
public final class IsoDateTimeParser {

    private static final Pattern IsoDate = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})$");
    private static final Pattern IsoDateZ = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})([-+Z].*)$");
    private static final Pattern IsoDateTimeHHMM = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$");
    private static final Pattern IsoDateTimeHHMMZ = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})([-+Z].*)$");
    private static final Pattern IsoDateTimeHHMMSS = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})$");
    private static final Pattern IsoDateTimeHHMMSSZ = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})([-+Z].*)$");
    private static final Pattern IsoDateTimeHHMMSSmmm = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3})$");

    private static final Pattern IsoDateTimeHHMMSSmmmZ = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3})([-+Z].*)$");

    private static final Pattern ZoneHour = Pattern.compile("^([-+]\\d{2})$");
    private static final Pattern ZoneHourMinute = Pattern.compile("^([-+]\\d{2}):?(\\d{2})$");
    private static final Pattern ZoneHourMinuteSecond = Pattern.compile("^([-+]\\d{2})(\\d{2})(\\d{2})$");

    private static final Pattern HasZone = Pattern.compile("^.*([-+]\\d{2}:\\d{2}:\\d{2}|Z)$");

    public static ZonedDateTime parse(String str, ZoneId tz) {
        var timeStr = normalize(str);
        if (hasExplicitZone(timeStr)) {
            return ZonedDateTime.parse(timeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        return ZonedDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME.withZone(tz));
    }

    private static String normalizeZone(String zone) {
        Matcher matcher = ZoneHour.matcher(zone);
        if (matcher.find()) {
            return String.format("%s:00:00", matcher.group(1));
        }
        matcher = ZoneHourMinute.matcher(zone);
        if (matcher.find()) {
            return String.format("%s:%s:00", matcher.group(1), matcher.group(2));
        }
        matcher = ZoneHourMinuteSecond.matcher(zone);
        if (matcher.find()) {
            return String.format("%s:%s:%s", matcher.group(1), matcher.group(2), matcher.group(3));
        }
        return zone;
    }

    private static String normalize(String str) {
        Matcher matcher = IsoDate.matcher(str);
        if (matcher.find()) {
            return String.format("%sT00:00:00", matcher.group(1));
        }
        matcher = IsoDateZ.matcher(str);
        if (matcher.find()) {
            return String.format("%sT00:00:00%s", matcher.group(1), normalizeZone(matcher.group(2)));
        }
        matcher = IsoDateTimeHHMM.matcher(str);
        if (matcher.find()) {
            return String.format("%s:00", matcher.group(1));
        }
        matcher = IsoDateTimeHHMMZ.matcher(str);
        if (matcher.find()) {
            return String.format("%s:00%s", matcher.group(1), normalizeZone(matcher.group(2)));
        }
        matcher = IsoDateTimeHHMMSS.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = IsoDateTimeHHMMSSZ.matcher(str);
        if (matcher.find()) {
            return matcher.group(1) + normalizeZone(matcher.group(2));
        }
        matcher = IsoDateTimeHHMMSSmmm.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = IsoDateTimeHHMMSSmmmZ.matcher(str);
        if (matcher.find()) {
            return matcher.group(1) + normalizeZone(matcher.group(2));
        }
        return str;
    }

    private static boolean hasExplicitZone(String str) {
        return HasZone.matcher(str).matches();
    }

    private IsoDateTimeParser() {
        // Do not instantiate.
    }
}
