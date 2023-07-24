package dev.byblos.chart.test;

import dev.byblos.chart.util.Fonts;
import dev.byblos.chart.util.Image;
import dev.byblos.chart.util.PngImage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class GraphAssertions {
    private final String goldenDir;
    private final String absoluteGoldenDir;
    private final String targetDir;
    private final BiConsumer<Object, Object> assertFn;

    public GraphAssertions(String goldenDir, String targetDir, BiConsumer<Object, Object> assertFn) {
        this.goldenDir = goldenDir;
        this.targetDir = targetDir;
        this.assertFn = assertFn;
        absoluteGoldenDir = new File(goldenDir).getAbsolutePath();
    }

    public void generateReport(Class<?> clazz) throws IOException {
        generateReport(clazz, true);
    }

    public void generateReport(Class<?> clazz, boolean diffsOnly) throws IOException {
        var report = "<html><head><title>" + clazz.getSimpleName() + "</title></head>";
        report += "<body><h1>" + clazz.getSimpleName() + "</h1><hr/>";
        var dir = new File(targetDir);
        dir.mkdirs();
        report += Arrays.stream(dir.listFiles())
                .filter(f -> f.getName().endsWith(".png"))
                .filter(f -> !f.getName().startsWith("diff_"))
                .map(f -> {
                    var diffImg = new File(targetDir + "/diff_" + f.getName());
                    if (diffsOnly && !diffImg.isFile()) {
                        return "";
                    }
                    var html = "<div><h2>" + f.getName() + "</h2>";
                    html += "<table border=\"1\">";
                    html += "<tr><th>Golden</th><th>Test</th><th>Diff</th></tr>";
                    html += "<tr valign=\"top\">";
                    html += "<td><img src=\"" + absoluteGoldenDir + "/" + f.getName() + "\"/></td>";
                    html += "<td><img src=\"" + f.getName() + "\"/></td>";
                    if (diffImg.isFile()) {
                        html += "<td><img src=\"diff_" + f.getName() + "\"/></td>";
                    } else {
                        html += "<td></td>";
                    }
                    html += "</tr></table></div>";
                    return html;
                })
                .collect(Collectors.joining());
        report += "</body></html>";

        Files.write(Paths.get(targetDir, "report.html"), report.getBytes(StandardCharsets.UTF_8));
    }

    public void assertEquals(Image i1, String f, boolean bless) throws IOException {
        assertEquals(i1.toPngImage(), f, bless);
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
        assertEquals(i1.toPngImage(), i2);
    }

    private void assertEquals(PngImage i1, PngImage i2) {
        var diff = PngImage.diff(i1.data(), i2.data());
        assertFn.accept(diff.metadata().get("identical"), "true");
        assertFn.accept(i1.metadata(), i2.metadata());
    }

    private void blessImage(Image img, String f) throws IOException {
        writeImage(img, goldenDir, f);
    }

    private PngImage getImage(String file) throws IOException {
        return PngImage.fromInputStream(getInputStream(file));
    }

    private InputStream getInputStream(String file) throws FileNotFoundException {
        return new FileInputStream(goldenDir + "/" + file);
    }

    private void writeImage(Image img, String dir, String f) throws IOException {
        var file = Paths.get(dir, f);
        Files.createDirectories(file.getParent());
        try (var stream = new FileOutputStream(file.toFile())) {
            img.write(stream);
        }
    }
}
