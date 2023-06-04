package dev.byblos.core.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Locale;

/**
 * Common prefixes used for units or human-readable strings.
 */
public final class UnitPrefix {
    private static final double maxValue = 1e27;
    private static final double minValue = 1e-27;
    // From Scala's Double: The smallest positive value greater than 0.0d which is representable as a Double.
    private static final double MinPositiveValue = 4.9E-324;


    public static final UnitPrefix one = new UnitPrefix("", "", 1.0);

    public static final UnitPrefix deca = new UnitPrefix("da", "deca", 1.0e1);
    public static final UnitPrefix hecto = new UnitPrefix("h", "hecto", 1.0e2);
    public static final UnitPrefix kilo = new UnitPrefix("k", "kilo", 1.0e3);
    public static final UnitPrefix mega = new UnitPrefix("M", "mega", 1.0e6);
    public static final UnitPrefix giga = new UnitPrefix("G", "giga", 1.0e9);
    public static final UnitPrefix tera = new UnitPrefix("T", "tera", 1.0e12);
    public static final UnitPrefix peta = new UnitPrefix("P", "peta", 1.0e15);
    public static final UnitPrefix exa = new UnitPrefix("E", "exa", 1.0e18);
    public static final UnitPrefix zetta = new UnitPrefix("Z", "zetta", 1.0e21);
    public static final UnitPrefix yotta = new UnitPrefix("Y", "yotta", 1.0e24);

    public static final UnitPrefix deci = new UnitPrefix("d", "deci", 1.0e-1);
    public static final UnitPrefix centi = new UnitPrefix("c", "centi", 1.0e-2);
    public static final UnitPrefix milli = new UnitPrefix("m", "milli", 1.0e-3);
    public static final UnitPrefix micro = new UnitPrefix("\u03BC", "micro", 1.0e-6);
    public static final UnitPrefix nano = new UnitPrefix("n", "nano", 1.0e-9);
    public static final UnitPrefix pico = new UnitPrefix("p", "pico", 1.0e-12);
    public static final UnitPrefix femto = new UnitPrefix("f", "femto", 1.0e-15);
    public static final UnitPrefix atto = new UnitPrefix("a", "atto", 1.0e-18);
    public static final UnitPrefix zepto = new UnitPrefix("z", "zepto", 1.0e-21);
    public static final UnitPrefix yocto = new UnitPrefix("y", "yocto", 1.0e-24);

    public static final UnitPrefix kibi = new UnitPrefix("Ki", "kibi", 1024.0);
    public static final UnitPrefix mebi = new UnitPrefix("Mi", "mebi", kibi.factor * 1024.0);
    public static final UnitPrefix gibi = new UnitPrefix("Gi", "gibi", mebi.factor * 1024.0);
    public static final UnitPrefix tebi = new UnitPrefix("Ti", "tebi", gibi.factor * 1024.0);
    public static final UnitPrefix pebi = new UnitPrefix("Pi", "pebi", tebi.factor * 1024.0);
    public static final UnitPrefix exbi = new UnitPrefix("Ei", "exbi", pebi.factor * 1024.0);
    public static final UnitPrefix zebi = new UnitPrefix("Zi", "zebi", exbi.factor * 1024.0);
    public static final UnitPrefix yobi = new UnitPrefix("Yi", "yobi", zebi.factor * 1024.0);

    public static final UnitPrefix picos = new UnitPrefix("ps", "picos", 1.0e-12);
    public static final UnitPrefix nanos = new UnitPrefix("ns", "nanos", 1.0e-9);
    public static final UnitPrefix micros = new UnitPrefix("\u03BCs", "micros", 1.0e-6);
    public static final UnitPrefix millis = new UnitPrefix("ms", "millis", 1.0e-3);
    public static final UnitPrefix sec = new UnitPrefix("s", "sec", 1.0);
    public static final UnitPrefix min = new UnitPrefix("m", "min", 60.0);
    public static final UnitPrefix hour = new UnitPrefix("h", "hour", 3600.0);
    public static final UnitPrefix day = new UnitPrefix("d", "day", hour.factor * 24);
    public static final UnitPrefix week = new UnitPrefix("w", "week", day.factor * 7);
    public static final UnitPrefix year = new UnitPrefix("y", "year", day.factor * 365);

    public static final List<UnitPrefix> binaryPrefixes = List.of(kibi, mebi, gibi, tebi, pebi, exbi, zebi, yobi);

    public static final List<UnitPrefix> durationSmallPrefixes = List.of(millis, micros, nanos, picos);
    public static final List<UnitPrefix> durationBigPrefixes = List.of(year, week, day, hour, min, sec);

    private static final List<UnitPrefix> decimalBigPrefixes = List.of(yotta, zetta, exa, peta, tera, giga, mega, kilo);
    private static final List<UnitPrefix> decimalSmallPrefixes = List.of(milli, micro, nano, pico, femto, atto, zepto, yocto);

    private static final List<UnitPrefix> binaryBigPrefixes = List.copyOf(Lists.reverse(binaryPrefixes));


    private final String symbol;
    private final String text;
    private final double factor;


    /**
     * Constructor.
     *
     * @param symbol the symbol shown for the prefix
     * @param text   text for the prefix
     * @param factor the multiplication factor for the prefix
     */
    public UnitPrefix(String symbol, String text, double factor) {
        this.symbol = symbol;
        this.text = text;
        this.factor = factor;
    }

    public String text() {
        return text;
    }

    public double factor() {
        return factor;
    }

    public String format(double value, String fmtstr) {
        return String.format(Locale.ENGLISH, fmtstr, value / factor, symbol);
    }

    public String format(double v, String fmtstr, String scifmt) {
        if (UnitPrefix.hasExtremeExponent(v)) {
            var fmt = (v >= 0.0) ? " " + scifmt : scifmt;
            return String.format(Locale.ENGLISH, fmt, v);
        }
        return format(v, fmtstr);
    }

    public String format(double v) {
        return format(v, "%.1f%s", "%.0e");
    }

    public UnitPrefix next() {
        return Lists.reverse(UnitPrefix.decimalSmallPrefixes).stream().filter(x -> x.factor > factor).findFirst().orElseGet(() -> {
            return Lists.reverse(UnitPrefix.decimalBigPrefixes).stream().filter(x -> x.factor > factor).findFirst().orElse(UnitPrefix.yotta);
        });
    }

    public UnitPrefix prevDurationPrefix() {
        if (this == UnitPrefix.sec) {
            return UnitPrefix.durationSmallPrefixes.stream().filter(x -> x.factor < factor).findFirst().orElse(this);
        }
        return UnitPrefix.durationBigPrefixes.stream().filter(x -> x.factor < factor).findFirst().orElse(this);
    }

    /**
     * Returns an appropriate decimal prefix for `value`.
     */
    public static UnitPrefix decimal(double value) {
        var v = Math.abs(value);
        if (MoreMath.isNearlyZero(v)) {
            return one;
        }
        if (!Double.isFinite(v)) {
            return one;
        }
        if (v >= kilo.factor) {
            return decimalBigPrefixes.stream().filter(x -> x.factor <= v).findFirst().orElse(yotta);
        }
        if (v < 1.0) {
            return decimalSmallPrefixes.stream().filter(x -> x.factor <= v).findFirst().orElse(yocto);
        }
        return one;
    }

    /**
     * Returns an appropriate binary prefix for `value`. If the value is less than 1, then we
     * fall back to using the decimal small prefixes. It is expected that binary prefixes would
     * only get used with data greater than or equal to a byte.
     */
    public static UnitPrefix binary(double value) {
        var v = Math.abs(value);
        if (MoreMath.isNearlyZero(v)) {
            return one;
        }
        if (!Double.isFinite(v)) {
            return one;
        }
        if (v >= kibi.factor) {
            return binaryBigPrefixes.stream().filter(x -> x.factor <= v).findFirst().orElse(yobi);
        }
        if (v < 1.0) {
            return decimalSmallPrefixes.stream().filter(x -> x.factor <= v).findFirst().orElse(yocto);
        }
        return one;
    }

    /**
     * Returns an appropriate duration prefix for `value`.
     */
    public static UnitPrefix duration(double value) {
        var v = Math.abs(value);
        if (MoreMath.isNearlyZero(v)) {
            return sec;
        }
        if (!Double.isFinite(v)) {
            return sec;
        }
        if (v >= sec.factor) {
            return durationBigPrefixes.stream().filter(x -> x.factor <= v).findFirst().orElse(year);
        }
        if (v < 1.0) {
            return durationSmallPrefixes.stream().filter(x -> x.factor <= v).findFirst().orElse(picos);
        }
        return sec;
    }

    /**
     * Returns an appropriate decimal prefix for `value`.
     */
    public static UnitPrefix forRange(double value, double digits) {
        var f = Math.pow(10.0, digits);
        var v = Math.abs(value);
        if (MoreMath.isNearlyZero(v)) {
            return one;
        }
        if (!Double.isFinite(v)) {
            return one;
        }
        if (withinRange(one, v, f)) {
            return one;
        }
        if (v >= kilo.factor / f) {
            return Lists.reverse(decimalBigPrefixes).stream().filter(p -> withinRange(p, v, f)).findFirst().orElse(yotta);
        }
        if (v < 1.0 / f) {
            return decimalSmallPrefixes.stream().filter(p -> withinRange(p, v, f)).findFirst().orElse(yocto);
        }
        return one;
    }

    /**
     * Returns an appropriate binary prefix for `value`.
     */
    public static UnitPrefix binaryRange(double value, double digits) {
        var f = Math.pow(10.0, digits);
        var v = Math.abs(value);
        if (MoreMath.isNearlyZero(v)) {
            return one;
        }
        if (!Double.isFinite(v)) {
            return one;
        }
        if (withinRange(one, v, f)) {
            return one;
        }
        if (v >= kibi.factor / f) {
            return binaryPrefixes.stream().filter(p -> withinRange(p, v, f)).findFirst().orElse(yobi);
        }
        if (v < 1.0 / f) {
            return decimalSmallPrefixes.stream().filter(p -> withinRange(p, v, f)).findFirst().orElse(yocto);
        }
        return one;
    }

    public static UnitPrefix durationRange(double v) {
        var a = Math.abs(v);
        if (MoreMath.isNearlyZero(a)) {
            return sec;
        }
        if (!Double.isFinite(a)) {
            return sec;
        }
        if (a < sec.factor && a >= 1) {
            return sec;
        }
        if (a >= sec.factor) {
            var last = sec;
            UnitPrefix found = null;
            for (var prefix : Lists.reverse(durationBigPrefixes)) {
                if (prefix != last) {
                    if (found == null && a >= last.factor && a < prefix.factor) {
                        found = last;
                    }
                    last = prefix;
                }
            }
            return (found == null) ? year : found;
        }
        if (a < 1.0) {
            return durationSmallPrefixes.stream().filter(p -> a > p.factor).findFirst().orElse(nanos);
        }
        return sec;
    }

    private static boolean withinRange(UnitPrefix prefix, double v, double f) {
        var a = Math.abs(v);
        return a >= prefix.factor / f && a < prefix.factor * f;
    }

    private static boolean hasExtremeExponent(double v) {
        var d = Math.abs(v);
        return Double.isFinite(v) && (isLarge(d) || isSmall(d));
    }


    private static boolean isLarge(double v) {
        return v >= maxValue;
    }

    private static boolean isSmall(double v) {
        return v <= minValue && v >= MinPositiveValue;
    }
}
