package dev.byblos.chart.test;

import dev.byblos.chart.util.Fonts;
import dev.byblos.chart.util.PngImage;

import java.awt.image.RenderedImage;
import java.io.*;
import java.util.function.BiConsumer;

public final class GraphAssertions {
    private final String goldenDir;
    private final String targetDir;
    private final BiConsumer<Object, Object> assertFn;

    public GraphAssertions(String goldenDir, String targetDir, BiConsumer<Object, Object> assertFn) {
        this.goldenDir = goldenDir;
        this.targetDir = targetDir;
        this.assertFn = assertFn;
    }

    public void assertEquals(PngImage i1, String f) throws IOException {
        assertEquals(i1, f, false);
    }

    public void assertEquals(PngImage i1, String f, boolean bless) throws IOException {
        if (bless) {
            blessImage(i1, f);
        }

        // Skip on systems with incompatible font rendering
        if (!Fonts.shouldRunTests()) {
            return;
        }

        PngImage i2;
        try {
            i2 = getImage(f);
        } catch (IOException e) {
            i2 = PngImage.error(e.getMessage(), 400, 300);
        }
        var diff = PngImage.diff(i1.data(), i2.data());
        writeImage(i1, targetDir, f);

        // For reporting we use the existence of the diff image to determine whether
        // to show an entry. Only create if there is a diff and remove old diffs if
        // it is now the same to avoid a false report based on an old diff image in
        // the workspace.
        if (!diff.metadata().get("identical").equals("true")) {
            writeImage(diff, targetDir, "diff_" + f);
        } else {
            new File(targetDir + "/diff_" + f).delete();
        }
        assertEquals(i1, i2);
    }

    private void assertEquals(PngImage i1, PngImage i2) {
        assertEquals(i1.data(), i2.data());
        assertFn.accept(i1.metadata(), i2.metadata());
    }

    private void assertEquals(RenderedImage i1, RenderedImage i2) {
        var diff = PngImage.diff(i1, i2);
        assertFn.accept(diff.metadata().get("identical"), "true");
    }

    private void blessImage(PngImage img, String f) throws IOException {
        writeImage(img, goldenDir, f);
    }

    private PngImage getImage(String file) throws IOException {
        return PngImage.fromInputStream(getInputStream(file));
    }

    private InputStream getInputStream(String file) throws FileNotFoundException {
        return new FileInputStream(goldenDir + "/" + file);
    }

    private void writeImage(PngImage img, String dir, String f) throws IOException {
        var file = new File(new File(dir), f);
        file.getParentFile().mkdirs();
        var stream = new FileOutputStream(file);
        img.write(stream);
    }
}
