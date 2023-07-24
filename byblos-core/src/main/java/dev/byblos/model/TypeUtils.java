package dev.byblos.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class TypeUtils {
    public static boolean isPresentationType(Object v) {
        return v instanceof StyleExpr || isTimeSeriesType(v);
    }

    public static StyleExpr asPresentationType(Object v) {
        if (v instanceof StyleExpr) {
            return (StyleExpr) v;
        }
        return new StyleExpr(asTimeSeriesType(v), Map.of());
    }

    public static boolean isTimeSeriesType(Object v) {
        return v instanceof TimeSeriesExpr || v instanceof String;
    }

    public static TimeSeriesExpr asTimeSeriesType(Object v) {
        if (v instanceof TimeSeriesExpr) {
            return (TimeSeriesExpr) v;
        }
        if (v instanceof String) {
            try {
                return new ConstantExpr(asDouble(v));
            } catch (NumberFormatException e) {
                return new DataExpr((String) v);
            }
        }
        throw new IllegalArgumentException("not a time series");
    }

    public static boolean isDouble(Object v) {
        if (v instanceof Double) {
            return true;
        }
        if (v instanceof String) {
            try {
                Double.parseDouble((String) v);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static double asDouble(Object v) {
        return Double.parseDouble((String) v);
    }

    public static boolean isInt(Object v) {
        if (v instanceof Integer) {
            return true;
        }
        if (v instanceof String) {
            try {
                Integer.parseInt((String) v);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static int asInt(Object v) {
        return Integer.parseInt((String) v);
    }

    public static boolean isString(Object v) {
        return v instanceof String;
    }

    public static String asString(Object v) {
        return (String) v;
    }

    public static boolean isStringList(Object v) {
        return v instanceof List && ((List<Object>) v).stream().allMatch(x -> x instanceof String);
    }

    public static List<String> asStringList(Object v) {
        return asStringStream(v).toList();
    }

    public static Stream<String> asStringStream(Object v) {
        if (v instanceof List) {
            return ((List<Object>) v).stream().map(Object::toString);
        }
        throw new IllegalArgumentException("not a string list");
    }

    private TypeUtils() {
        // Do not instantiate.
    }
}
