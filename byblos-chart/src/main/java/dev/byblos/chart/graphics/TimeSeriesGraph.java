package dev.byblos.chart.graphics;

import dev.byblos.chart.GraphConstants;
import dev.byblos.chart.model.GraphDef;
import dev.byblos.chart.model.LineDef;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

public final class TimeSeriesGraph implements Element, FixedHeight, FixedWidth {
    /**
     * Allow at least 4 small characters on the right side to prevent the final tick mark label
     * from getting truncated.
     */
    private static final int minRightSidePadding = ChartSettings.smallFontDims.width() * 4;

    private final GraphDef graphDef;
    private final long start;
    private final long end;
    private final List<TimeAxis> timeAxes;
    private final List<ValueAxis> yAxes;

    public TimeSeriesGraph(GraphDef graphDef) {
        this.graphDef = requireNonNull(graphDef);
        start = graphDef.startTime().toEpochMilli();
        end = graphDef.endTime().toEpochMilli();
        timeAxes = IntStream.range(0, graphDef.timezones().size()).boxed().map(i -> {
            return new TimeAxis(
                    Style.create(graphDef.theme().axis().line().color()),
                    start,
                    end,
                    graphDef.step(),
                    graphDef.timezones().get(i),
                    (i == 0) ? 40 : 0xFF,
                    true
            );
        }).collect(Collectors.toList());
        yAxes = IntStream.range(0, graphDef.plots().size()).boxed().map(i -> {
            var plot = graphDef.plot(i);
            var bounds = plot.bounds(start, end);
            if (i == 0) {
                return new LeftValueAxis(plot, graphDef.theme().axis(), bounds.min(), bounds.max());
            }
            return new RightValueAxis(plot, graphDef.theme().axis(), bounds.min(), bounds.max());
        }).collect(Collectors.toList());
    }

    @Override
    public int height() {
        var max = GraphConstants.MaxHeight;
        int h;
        if (graphDef.height() > max) {
            h = max;
        } else {
            var min = GraphConstants.MinCanvasHeight;
            h = Math.max(graphDef.height(), min);
        }
        if (graphDef.onlyGraph() || graphDef.layout().isFixedHeight()) {
            return h;
        }
        return h + timeAxes.stream().mapToInt(TimeAxis::height).sum();
    }

    @Override
    public int width() {
        var max = GraphConstants.MaxWidth;
        int w;
        if (graphDef.width() > max) {
            w = max;
        } else {
            var min = GraphConstants.MinCanvasWidth;
            w = Math.max(graphDef.width(), min);
        }
        if (graphDef.onlyGraph() || graphDef.layout().isFixedWidth()) {
            return w;
        }
        var rightPadding = (yAxes.size() > 1) ? 0 : minRightSidePadding;
        return w + yAxes.stream().mapToInt(ValueAxis::width).sum() + rightPadding;
    }

    @Override
    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        var leftAxisW = yAxes.get(0).width();
        var rightAxisW = yAxes.stream().skip(1).mapToInt(ValueAxis::width).sum();
        var rightSideW = (rightAxisW > 0) ? rightAxisW : TimeSeriesGraph.minRightSidePadding;
        var axisW = leftAxisW + rightSideW;
        var width = x2 - x1 - axisW;

        var showAxes = !graphDef.onlyGraph() && width >= GraphConstants.MinCanvasWidth;
        var leftOffset = (showAxes) ? leftAxisW : TimeSeriesGraph.minRightSidePadding;
        var rightOffset = (showAxes) ? rightSideW : TimeSeriesGraph.minRightSidePadding;

        var timeAxis = timeAxes.get(0);
        var timeAxisH = (graphDef.onlyGraph()) ? 10 : timeAxis.height();
        var timeGrid = new TimeGrid(timeAxis, graphDef.theme().majorGrid().line(), graphDef.theme().minorGrid().line());

        var chartEnd = y2 - timeAxisH * timeAxes.size();

        var prevClip = g.getClip();
        clip(g, x1 + leftOffset, y1, x2 - rightOffset, chartEnd + 1);
        for (var i = 0; i < graphDef.plots().size(); i++) {
            var plot = graphDef.plots().get(i);
            var axis = yAxes.get(i);
            var offsets = TimeSeriesStack.Offsets.fromAxis(timeAxis);
            for (var line : plot.lines()) {
                var lineElement = getLineElement(line, timeAxis, axis, offsets);
                lineElement.draw(g, x1 + leftOffset, y1, x2 - rightOffset, chartEnd);
            }

            for (var hspan : plot.horizontalSpans()) {
                var style = Style.create(hspan.color());
                var spanElement = new ValueSpan(style, hspan.v1(), hspan.v2(), axis);
                spanElement.draw(g, x1 + leftOffset, y1, x2 - rightOffset, chartEnd);
            }

            for (var vspan : plot.verticalSpans()) {
                var style = Style.create(vspan.color());
                var spanElement = new TimeSpan(style, vspan.t1().toEpochMilli(), vspan.t2().toEpochMilli(), timeAxis);
                spanElement.draw(g, x1 + leftOffset, y1, x2 - rightOffset, chartEnd);
            }
        }
        g.setClip(prevClip);

        timeGrid.draw(g, x1 + leftOffset, y1, x2 - rightOffset, chartEnd);

        if (!graphDef.onlyGraph()) {
            for (var i = 0; i < timeAxes.size(); i++) {
                var offset = chartEnd + 1 + timeAxisH * i;
                timeAxes.get(i).draw(g, x1 + leftOffset, offset, x2 - rightOffset, y2);
            }
        }

        var valueGrid = new ValueGrid(yAxes.get(0), graphDef.theme().majorGrid().line(), graphDef.theme().minorGrid().line());
        valueGrid.draw(g, x1 + leftOffset, y1, x2 - rightOffset, chartEnd);
        if (showAxes) {
            yAxes.get(0).draw(g, x1, y1, x1 + leftAxisW - 1, chartEnd);
            for (var i = 1; i < yAxes.size(); i++) {
                var offset = leftAxisW + width + leftAxisW * i;
                yAxes.get(i).draw(g, x1 + offset, y1, x1 + offset + leftAxisW, chartEnd);
            }
        }
    }

    private void clip(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.setClip(x1, y1, x2 - x1, y2 - y1);
        g.setColor(graphDef.theme().canvas().background().color());
        g.fillRect(x1, y1, x2 - x1, y2 - y1);
    }

    private Element getLineElement(LineDef line, TimeAxis timeAxis, ValueAxis axis, TimeSeriesStack.Offsets offsets) {
        var style = new Style(line.color(), new BasicStroke(line.lineWidth()));
        switch (line.lineStyle()) {
            case LINE:
                return new TimeSeriesLine(style, line.data().data(), timeAxis, axis);
            case AREA:
                return new TimeSeriesArea(style, line.data().data(), timeAxis, axis);
            case VSPAN:
                return new TimeSeriesSpan(style, line.data().data(), timeAxis);
            case STACK:
                return new TimeSeriesStack(style, line.data().data(), timeAxis, axis, offsets);
            default:
                throw new AssertionError();
        }
    }
}