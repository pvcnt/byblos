package dev.byblos.chart.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.zone.ZoneOffsetTransition;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Draws a time based X-axis.
 */
public final class TimeAxis implements Element, FixedHeight {

    private static final int minTickLabelWidth = " 00:00 ".length() * ChartSettings.smallFontDims.width();

    private final Style style;
    private final long start;
    private final long end;
    private final long step;
    private final ZoneId zone; // Default: ZoneOffset.UTC
    private final int alpha; // Default: 40
    private final boolean showZone; // Default: true
    private final ZoneOffsetTransition transition;
    private final long transitionTime;

    /**
     * Constructor.
     *
     * @param style    Style to use for the axis and corresponding labels.
     * @param start    Start time in milliseconds since the epoch.
     * @param end      End time in milliseconds since the epoch.
     * @param step     Step size in milliseconds.
     * @param zone     Time zone to use for the labels. This is a presentation detail only and can sometimes
     *                 result in duplicates. For example, during a daylight savings transition the same hour
     *                 can be used for multiple tick marks. Defaults to UTC.
     * @param alpha    Alpha setting to use for the horizontal line of the axis. If the time axis is right next to
     *                 the chart, then increasing the transparency can help make it easier to see lines that are
     *                 right next to axis. Defaults to 40.
     * @param showZone If set to true, then the abbreviation for the time zone will be shown to the left of the
     *                 axis labels.
     */
    public TimeAxis(Style style, long start, long end, long step, ZoneId zone, int alpha, boolean showZone) {
        this.style = style;
        this.start = start;
        this.end = end;
        this.step = step;
        this.zone = zone;
        this.alpha = alpha;
        this.showZone = showZone;
        var s = Instant.ofEpochMilli(start);
        transition = zone.getRules().nextTransition(s);
        transitionTime = (transition == null) ? Long.MAX_VALUE : transition.getInstant().toEpochMilli();
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }

    public long step() {
        return step;
    }

    public Scales.LongScale scale(int p1, int p2) {
        return Scales.time(start - step, end - step, step, p1, p2);
    }

    public List<TimeTick> ticks(int x1, int x2) {
        // The first interval displayed will end at the start time. For calculating ticks the
        // start time is adjusted so we can see minor ticks within the first interval
        var numTicks = (x2 - x1) / TimeAxis.minTickLabelWidth;
        return Ticks.time(start - step, end, zone, numTicks);
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        var txtH = ChartSettings.smallFontDims.height();
        var labelPadding = TimeAxis.minTickLabelWidth / 2;

        // Horizontal line across the bottom of the chart. The main horizontal line for the axis is
        // made faint so it is easier to see lines in the chart that are directly against the axis.
        style.withAlpha(alpha).configure(g);
        g.drawLine(x1, y1, x2, y1);

        style.configure(g);
        var xscale = scale(x1, x2);
        var majorTicks = ticks(x1, x2).stream().filter(TimeTick::major).collect(Collectors.toList());
        var indicatedTransition = false;
        for (var tick : majorTicks) {
            var px = xscale.apply(tick.timestamp());
            if (px >= x1 && px <= x2) {
                // Vertical tick mark
                g.drawLine(px, y1, px, y1 + 4);

                // Label for the tick mark
                if (tick.timestamp() >= transitionTime && !indicatedTransition) {
                    indicatedTransition = true;
                    var before = transition.getOffsetBefore();
                    var after = transition.getOffsetAfter();
                    var delta = Duration.ofSeconds(after.getTotalSeconds() - before.getTotalSeconds());
                    var label = (delta.isNegative() ? "" : "+") + delta.toString().substring(2);
                    var txt = Text.center(label, ChartSettings.smallFont, style.withColor(Color.RED));
                    txt.draw(g, px - labelPadding, y1 + txtH / 2, px + labelPadding, y1 + txtH);
                } else {
                    var txt = Text.center(tick.label(), ChartSettings.smallFont, style);
                    txt.draw(g, px - labelPadding, y1 + txtH / 2, px + labelPadding, y1 + txtH);
                }
            }
        }

        // Show short form of time zone as a label for the axis
        if (showZone) {
            var name = zone.getDisplayName(TextStyle.NARROW_STANDALONE, Locale.US);
            var zoneLabel = Text.right(name, ChartSettings.smallFont, style);
            var labelW = (name.length() + 2) * ChartSettings.smallFontDims.width();
            var padding = labelPadding + 2;
            zoneLabel.draw(g, x1 - labelW - padding, y1 + txtH / 2, x1 - padding, y1 + txtH);
        }
    }

    @Override
    public int height() {
        return 10 + ChartSettings.smallFontDims.height();
    }
}