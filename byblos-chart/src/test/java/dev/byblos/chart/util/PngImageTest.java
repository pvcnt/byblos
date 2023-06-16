package dev.byblos.chart.util;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PngImageTest {
    @Test
    void loadImage() throws Exception {
        var image = getImage("test.png");
        assertEquals(image.metadata().size(), 2);
        assertEquals(image.metadata().get("identical"), "false");
        assertEquals(image.metadata().get("diff-pixel-count"), "48302");
    }

    @Test
    void diffImageWhenIdentical() {
        var i1 = PngImage.error("", 800, 100);
        var i2 = PngImage.error("", 800, 100);
        var diff = PngImage.diff(i1.data(), i2.data());
        assertEquals(diff.metadata().get("identical"), "true");
        assertEquals(diff.metadata().get("diff-pixel-count"), "0");
    }

    @Test
    void diffImageWithDelta() {
        var i1 = PngImage.error("", 800, 100);
        var i2 = PngImage.error("", 801, 121);
        var diff = PngImage.diff(i1.data(), i2.data());
        assertEquals(diff.metadata().get("identical"), "false");
        assertEquals(diff.metadata().get("diff-pixel-count"), "16921");
    }

    @Test
    void userErrorImageIsDifferentThanSystemErrorImage() {
        var userErrorImage = PngImage.userError("", 800, 100);
        var systemErrorImage = PngImage.systemError("", 800, 100);
        var diff = PngImage.diff(userErrorImage.data(), systemErrorImage.data());

        assertEquals(diff.metadata().get("identical"), "false");
        assertEquals(diff.metadata().get("diff-pixel-count"), "80000");
    }

    @Test
    public void imageMetadata() throws Exception {
        var metadata = Map.of(
                "english", "source url",
                "japanese", "ソースURL",
                "compressed", IntStream.range(0, 10000).mapToObj(String::valueOf).collect(Collectors.joining(","))
        );
        var img = PngImage.error("test", 100, 100).withMetadata(metadata);

        var baos = new ByteArrayOutputStream();
        img.write(baos);
        var decoded = PngImage.fromBytes(baos.toByteArray());

        assertEquals(metadata, decoded.metadata());
    }

    private InputStream getInputStream(String file) throws IOException {
        return Resources.getResource("pngimage/" + file).openStream();
    }

    private PngImage getImage(String file) throws IOException {
        return PngImage.fromInputStream(getInputStream(file));
    }
}
