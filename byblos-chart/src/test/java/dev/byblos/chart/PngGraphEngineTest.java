package dev.byblos.chart;

public class PngGraphEngineTest extends ImageGraphEngineTest {
    private final static PngGraphEngine ENGINE = new PngGraphEngine();

    @Override
    public String getPrefix() { return "default"; }

    @Override
    public ImageGraphEngine graphEngine() {
        return ENGINE;
    }
}
