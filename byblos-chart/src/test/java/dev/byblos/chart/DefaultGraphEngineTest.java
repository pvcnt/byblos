package dev.byblos.chart;

public class DefaultGraphEngineTest extends PngGraphEngineTest {
    private final static DefaultGraphEngine ENGINE = new DefaultGraphEngine();

    @Override
    public String getPrefix() { return "default"; }

    @Override
    public PngGraphEngine graphEngine() {
        return ENGINE;
    }
}
