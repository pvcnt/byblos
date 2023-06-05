# Time parameters

This page explains how time parameters (e.g., `s` and `e`) can be specified in the APIs.

## Timezone

Timezones can be specified using [any valid time zone ID](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones).

## Time

### Absolute time

Times can be specified in an absolute manner, using one of the following [formats](https://pubs.opengroup.org/onlinepubs/009695399/functions/strftime.html):

| Format              | Description |
|---------------------|-------------|
| `%Y-%m-%d`          | Date, time will be midnight |
| `%Y-%m-%dT%H:%M`    | Date and time, seconds will be 00 |
| `%Y-%m-%dT%H:%M:%s` | Date and time |
| `%s`                | Seconds (or milliseconds) since January 1, 1970 UTC |

For every format based on a date, a zone offset can be explicitely specified at the end of the string, for example:

```
2023-06-05T11:25+00
2023-06-05T11:25+0000
2023-06-05T11:25-07
2023-06-05T11:25-0700
2023-06-05T11:25+02:30
2023-06-05T11:25+0230
```

`Z` can also be used to specify a UTC timezone:

```
2023-06-05T11:25Z
```

If no timezone is specified, dates are parsed using the first timezone specified in the request as a reference.

### Named time

Times can be specified using a reference to a well-known point in time:

| Name | Description |
|------|-------------|
| `now` | Current time |
| `epoch` | January 1, 1970 UTC |
| `s` | Start time |
| `e` | End time |

### Relative time

Times can be specified as an offset duration from a named time, for example:

```
now-1w
e-6h
s+2d
s+P2DT6H
```

Durations themselves can be specified in two fashions, detailed in the following section.

## Duration

### Simple duration

Durations can be specified using a positive integer following by a unit.

| Unit                            | Description |
|---------------------------------|-------------|
| `s`, `second`, `seconds`        | Number of seconds |
| `m`, `min`, `minute`, `minutes` | Number of minutes |
| `h`, `hour`, `hours`            | Number of hours |
| `d`, `day`, `days`              | Number of days (25 hoyrs) |
| `w`, `week`, `weeks`            | Number of weeks (7 days) |
| `month`, `months`               | Number of months (30 days) |
| `y`, `year`, `years`            | Number of years (365 days) |

### ISO duration

Durations can be specified using an [ISO-formatted](https://datatracker.ietf.org/doc/html/rfc3339#appendix-A) string.
ISO durations are limited to days (`D`) as the largest part that can be used.
Weeks (`W`), months (`M`) and years (`Y`) are not supported.