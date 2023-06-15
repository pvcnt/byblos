package dev.byblos.chart.model;

import dev.byblos.core.model.SummaryStats;
import dev.byblos.core.model.TimeSeries;
import org.immutables.value.Value;

import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Definition for a time series line.
 */
@Value.Immutable
@Value.Style(overshadowImplementation = true)
public abstract class LineDef implements DataDef {

    /**
     * Return the time series with the underlying data to render.
     */
    public abstract TimeSeries data();

    /**
     * Return the expression for the time series. Note, the same expression can result in many time
     * series when using group by. For matching the data for a particular time series the
     * id field should be used.
     */
    public abstract Optional<String> query();

    /**
     * Return the color to use when rendering the line.
     */
    @Override
    @Value.Default
    public Color color() {
        return Color.RED;
    }

    /**
     * Return the style to use when rendering.
     */
    @Value.Default
    public LineStyle lineStyle() {
        return LineStyle.LINE;
    }

    /**
     * Return the width of the stroke when rendering the line. Has no effect for
     * styles other than {@link LineStyle#LINE}.
     */
    @Value.Default
    public float lineWidth() {
        return 1.0f;
    }

    /**
     * Return the summary stats for the data in the line.
     */
    @Value.Default
    public SummaryStats legendStats() {
        return SummaryStats.EMPTY;
    }

    @Override
    public String label() {
        return data().label();
    }

    public ImmutableLineDef.Builder toBuilder() {
        return ImmutableLineDef.builder().from(this);
    }
}