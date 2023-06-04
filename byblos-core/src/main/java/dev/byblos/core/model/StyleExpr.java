package dev.byblos.core.model;

import com.google.common.base.Splitter;
import dev.byblos.core.util.Strings;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public record StyleExpr(TimeSeriesExpr expr, Map<String, String> settings) implements Expr {
    public long offset() {
        return expr.dataExprs().stream()
                .mapToLong(e -> e.offset().toMillis())
                .min()
                .orElse(0L);
    }

    public String legend(TimeSeries t) {
        return legend(t.label(), t.tags());
    }

    public String legend(String label, Map<String, String> tags) {
        var fmt = settings.getOrDefault("legend", label);
        return sed(Strings.substitute(fmt, tags));
    }

    public Optional<String> palette() {
        return Optional.ofNullable(settings.get("palette"));
    }

    public Optional<String> color() {
        return Optional.ofNullable(settings.get("color"));
    }

    public Optional<String> lineStyle() {
        return Optional.ofNullable(settings.get("ls"));
    }

    public Optional<String> sortBy() {
        return Optional.ofNullable(settings.get("sort"));
    }

    public boolean useDescending() {
        return "desc".equals(settings.get("sort"));
    }

    public Float lineWidth() {
        return Optional.ofNullable(settings.get("lw")).map(Float::parseFloat).orElse(1.0f);
    }

    public Optional<Integer> alpha() {
        return Optional.ofNullable(settings.get("alpha")).map(s -> {
            checkArgument(s.length() == 2, "value should be 2 digit hex string");
            return Integer.parseInt(s, 16);
        });
    }

    /**
     * Returns the maximum number of lines that should be shown for this expression.
     */
    public Optional<Integer> limit() {
        return Optional.ofNullable(settings.get("limit")).map(Integer::parseInt);
    }

    public Optional<Integer> axis() {
        return Optional.ofNullable(settings.get("axis")).map(Integer::parseInt);
    }

    @Override
    public String toString() {
        if (settings.isEmpty()) {
            return expr.toString();
        }
        // Use descending order to ensure that an explicit alpha used with
        // a palette is not overwritten when reprocessing the expression string.
        // This works because palette will be sorted before alpha, though a better
        // future solution would be to use a map that preserves the order of
        // updates to the object.
        var vs = settings.entrySet()
                .stream()
                .sorted(Comparator.comparing((Map.Entry<String, String> e) -> e.getKey()).reversed())
                .map(e -> "sed".equals(e.getKey()) ? e.getValue() : String.format("%s,:%s", e.getValue(), e.getKey()))
                .collect(Collectors.joining(","));
        return expr.toString() + "," + vs;
    }

    private String sed(String str) {
        var sed = settings.get("sed");
        if (null == sed) {
            return str;
        }
        return sed(str, Splitter.on(",").splitToList(sed));
    }

    private String sed(String str, List<String> cmds) {
        if (cmds.size() == 0) {
            return str;
        }
        if (cmds.size() >= 2 && ":decode".equals(cmds.get(1))) {
            return sed(decode(str, cmds.get(0)), cmds.subList(2, cmds.size()));
        }
        if (cmds.size() >= 3 && ":s".equals(cmds.get(2))) {
            return sed(searchAndReplace(str, cmds.get(0), cmds.get(1)), cmds.subList(3, cmds.size()));
        }
        return sed(str, cmds.subList(1, cmds.size()));
    }

    private String decode(String str, String mode) {
        switch (mode) {
            case "hex":
                return Strings.hexDecode(str, '_');
            case "none":
                return str;
            default:
                throw new IllegalArgumentException(String.format("unknown encoding '%s'", mode));
        }
    }

    private String searchAndReplace(String str, String search, String replace) {
        var matcher = Pattern.compile(search).matcher(str);
        if (!matcher.find()) {
            return str;
        }
        var sb = new StringBuilder();
        matcher.appendReplacement(sb, substitute(replace, matcher));
        while (matcher.find()) {
            matcher.appendReplacement(sb, substitute(replace, matcher));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * The `appendReplacement` method on the matcher will do substitutions, but this makes
     * it consistent with the variable substitutions for legends to avoid confusion about
     * slightly different syntax for variables in legends verses the replacement field.
     */
    private String substitute(String str, Matcher matcher) {
        return Strings.substitute(str, k -> isNumber(k) ? matcher.group(Integer.parseInt(k)) : matcher.group(k));
    }

    private static final Pattern numberPattern = Pattern.compile("^(\\d+)$");

    private boolean isNumber(String s) {
        return numberPattern.matcher(s).matches();
    }
}
