package dev.byblos.eval.graph;

import com.google.common.base.Functions;
import com.typesafe.config.Config;
import dev.byblos.chart.GraphEngine;
import dev.byblos.core.model.CustomVocabulary;
import dev.byblos.core.stacklang.Interpreter;
import dev.byblos.core.stacklang.Vocabulary;
import dev.byblos.core.util.Strings;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Default settings to use when rendering a graph image.
 */
public final class DefaultSettings {
    private final Config config;
    private final long stepSize;
    private final String startTime;
    private final String endTime;
    private final String timezone;
    private final int width;
    private final int height;
    private final String theme;
    private final boolean metadataEnabled;
    private final int maxDatapoints;
    private final Pattern browserAgentPattern;
    private final Map<String, GraphEngine> engines;
    private final Map<String, String> contentTypes;
    private final Vocabulary vocabulary;
    private final Interpreter interpreter;

    /**
     * Constructor.
     *
     * @param root   The full config object for the app. Primarily used for constructing
     *               any custom vocabulary that is needed for the interpreter.
     * @param config The specific config block for graph settings. This is typically under
     *               `byblos.eval.graph`.
     */
    private DefaultSettings(Config root, Config config) {
        this.config = requireNonNull(config);
        stepSize = config.getDuration("step", TimeUnit.MILLISECONDS);
        startTime = config.getString("start-time");
        endTime = config.getString("end-time");
        timezone = config.getString("timezone");
        width = config.getInt("width");
        height = config.getInt("height");
        theme = config.getString("theme");
        metadataEnabled = config.getBoolean("png-metadata-enabled");
        maxDatapoints = config.getInt("max-datapoints");
        browserAgentPattern = Pattern.compile(config.getString("browser-agent-pattern"), Pattern.CASE_INSENSITIVE);
        engines = config.getStringList("engines")
                .stream()
                .map(DefaultSettings::<GraphEngine>newInstance)
                .collect(Collectors.toMap(GraphEngine::name, Functions.identity()));
        contentTypes = engines.entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().contentType()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        vocabulary = createVocabulary(root);
        interpreter = new Interpreter(vocabulary.allWords());
    }

    public static DefaultSettings fromConfig(Config root) {
        return new DefaultSettings(root, root.getConfig("byblos.eval.graph"));
    }

    /**
     * Returns default step size to use for the chart. This should typically match the primary step
     * size of the underlying storage.
     */
    public long stepSize() {
        return stepSize;
    }

    /**
     * Returns default start time for the chart. This value should typically be relative to the
     * end time.
     */
    public String startTime() {
        return startTime;
    }

    /**
     * Returns default end time for the chart. This value should typically be relative to `now`.
     */
    public String endTime() {
        return endTime;
    }

    /**
     * Returns default time zone for the chart.
     */
    public String timezone() {
        return timezone;
    }

    /**
     * Returns default width for the chart.
     */
    public int width() {
        return width;
    }

    /**
     * Returns default height for the chart.
     */
    public int height() {
        return height;
    }

    /**
     * Returns default theme to use for the chart.
     */
    public String theme() {
        return theme;
    }

    /**
     * Returns default palette name to use.
     */
    public String primaryPalette(String theme) {
        return config.getString(theme + ".palette.primary");
    }

    /**
     * Returns default palette name to use for lines with an offset.
     */
    public String offsetPalette(String theme) {
        return config.getString(theme + ".palette.offset");
    }

    /**
     * Resolve color for a given theme.
     */
    public Color resolveColor(String theme, String color) {
        var k = theme + ".named-colors." + color;
        return Strings.parseColor(config.hasPath(k) ? config.getString(k) : color);
    }

    /**
     * Returns whether the uri and other graph metadata be encoded as text fields in the image.
     */
    public boolean metadataEnabled() {
        return metadataEnabled;
    }

    /**
     * Returns whether a user-agent is a web-browser.
     */
    public boolean isBrowserAgent(String agent) {
        return browserAgentPattern.matcher(agent).find();
    }

    /**
     * Returns maximum number of datapoints allowed for a line in a chart.
     */
    public int maxDatapoints() {
        return maxDatapoints;
    }

    /**
     * Returns available engines for rendering a chart.
     */
    public Map<String, GraphEngine> engines() {
        return engines;
    }

    /**
     * Returns content types for the various rendering options.
     */
    public Map<String, String> contentTypes() {
        return contentTypes;
    }

    /**
     * Returns the interpreter for the graph expressions.
     */
    public Interpreter interpreter() {
        return interpreter;
    }

    /**
     * Returns the vocabulary used by the interpreter.
     */
    public Vocabulary vocabulary() {
        return vocabulary;
    }

    private Vocabulary createVocabulary(Config root) {
        var vocabularyName = config.getString("vocabulary");
        if (vocabularyName.equals("default")) {
            return new CustomVocabulary(root);
        }
        return newInstance(vocabularyName);
    }

    private static <T> T newInstance(String cls) {
        try {
            return (T) Class.forName(cls).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("error while instantiating %s", cls), e);
        }
    }
}
