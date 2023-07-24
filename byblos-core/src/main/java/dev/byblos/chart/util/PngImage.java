package dev.byblos.chart.util;

import com.google.common.io.Closeables;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.text.AttributedString;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;

public record PngImage(RenderedImage data, Map<String, String> metadata) implements Image {

    static {
        // Disable using on-disk cache for images. Avoids temp files on shared services.
        ImageIO.setUseCache(false);
    }

    // Should we use antialiasing? This will typically need to be disabled for tests to
    // get reliable image comparisons.
    public static boolean useAntiAliasing = true;

    @Override
    public PngImage toPngImage() {
        return this;
    }

    @Override
    public PngImage withMetadata(Map<String, String> metadata) {
        return new PngImage(data, metadata);
    }

    public static PngImage fromBytes(byte[] bytes) throws IOException {
        return fromInputStream(new ByteArrayInputStream(bytes));
    }

    public static PngImage fromInputStream(InputStream input) throws IOException {
        try {
            var iterator = ImageIO.getImageReadersBySuffix("png");
            checkState(iterator.hasNext(), "no image readers for png");

            var reader = iterator.next();
            reader.setInput(ImageIO.createImageInputStream(input), true);

            var index = 0;
            var metadata = reader.getImageMetadata(index);
            var fields = extractTxtFields(metadata);
            var image = reader.read(index);
            return new PngImage(image, fields);
        } finally {
            Closeables.closeQuietly(input);
        }
    }

    public static PngImage diff(RenderedImage img1, RenderedImage img2) {
        var bi1 = newBufferedImage(img1);
        var bi2 = newBufferedImage(img2);

        var dw = Math.max(img1.getWidth(), img2.getWidth());
        var dh = Math.max(img1.getHeight(), img2.getHeight());

        var diffImg = newBufferedImage(dw, dh);
        var g = diffImg.createGraphics();
        g.setPaint(Color.BLACK);
        g.fill(new Rectangle(0, 0, dw, dh));

        var red = Color.RED.getRGB();

        var count = 0;
        var x = 0;
        while (x < dw) {
            var y = 0;
            while (y < dh) {
                if (contains(bi1, x, y) && contains(bi2, x, y)) {
                    var c1 = bi1.getRGB(x, y);
                    var c2 = bi2.getRGB(x, y);
                    if (c1 != c2) {
                        diffImg.setRGB(x, y, red);
                        count += 1;
                    }
                } else {
                    diffImg.setRGB(x, y, red);
                    count += 1;
                }
                y += 1;
            }
            x += 1;
        }

        var meta = Map.of("identical", String.valueOf((count == 0)), "diff-pixel-count", String.valueOf(count));
        return new PngImage(diffImg, meta);
    }

    public static PngImage userError(String imgText, int width, int height) {
        var userErrorYellow = new Color(0xFF, 0xCF, 0x00);
        return error(imgText, width, height, "USER ERROR:", Color.BLACK, userErrorYellow);
    }

    public static PngImage systemError(String imgText, int width, int height) {
        var systemErrorRed = new Color(0xF8, 0x20, 0x00);
        return error(imgText, width, height, "SYSTEM ERROR:", Color.WHITE, systemErrorRed);
    }

    public static PngImage error(String imgText, int width, int height) {
        return error(imgText, width, height, "ERROR:", Color.WHITE, Color.BLACK);
    }

    public static PngImage error(
            String imgText,
            int width,
            int height,
            String imgTextPrefix,
            Color imgTextColor,
            Color imgBackgroundColor
    ) {
        var fullMsg = imgTextPrefix + " " + imgText;

        var image = newBufferedImage(width, height);
        var g = image.createGraphics();

        if (useAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // Try to avoid problems with different default fonts on various platforms. Java will use the
        // "Dialog" font by default which can get mapped differently on various systems. It looks like
        // passing a bad font name into the font constructor will just silently fall back to the
        // default so it should still function if this font isn't present. Uses a default font that
        // is included as part of this library.
        var font = Fonts.DEFAULT;
        g.setFont(font);

        g.setPaint(imgBackgroundColor);
        g.fill(new Rectangle(0, 0, width, height));

        g.setPaint(imgTextColor);
        var attrStr = new AttributedString(fullMsg);
        attrStr.addAttribute(TextAttribute.FONT, font);
        var iterator = attrStr.getIterator();
        var measurer = new LineBreakMeasurer(iterator, g.getFontRenderContext());

        var wrap = width - 8.0f;
        var y = 0.0f;
        while (measurer.getPosition() < fullMsg.length()) {
            var layout = measurer.nextLayout(wrap);
            y += layout.getAscent();
            layout.draw(g, 4.0f, y);
            y += layout.getDescent() + layout.getLeading();
        }

        return new PngImage(image, Map.of());
    }

    private static BufferedImage newBufferedImage(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    private static BufferedImage newBufferedImage(RenderedImage img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        var w = img.getWidth();
        var h = img.getHeight();
        var bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        var g = bi.createGraphics();
        g.drawRenderedImage(img, new AffineTransform());
        return bi;
    }

    private static boolean contains(RenderedImage img, int x, int y) {
        return x < img.getWidth() && y < img.getHeight();
    }

    private static Map<String, String> extractTxtFields(IIOMetadata m) {
        var elements = ((IIOMetadataNode) m.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName))
                .getElementsByTagName("TextEntry");
        return IntStream.range(0, elements.getLength())
                .mapToObj(i -> (IIOMetadataNode) elements.item(i))
                .collect(Collectors.toMap(n -> n.getAttribute("keyword"), n -> n.getAttribute("value")));
    }

    @Override
    public void write(OutputStream output) throws IOException {
        var iterator = ImageIO.getImageWritersBySuffix("png");
        checkState(iterator.hasNext(), "no image writers for png");

        while (iterator.hasNext()) {
            var writer = iterator.next();
            try (var imageOutput = ImageIO.createImageOutputStream(output)) {
                writer.setOutput(imageOutput);
                var pngMeta = writer.getDefaultImageMetadata(new ImageTypeSpecifier(data), null);
                for (var entry : metadata.entrySet()) {
                    var textEntry = new IIOMetadataNode("TextEntry");
                    textEntry.setAttribute("keyword", entry.getKey());
                    textEntry.setAttribute("value", entry.getValue());
                    textEntry.setAttribute("compression", (entry.getValue().length() > 100) ? "zip" : "none");

                    var text = new IIOMetadataNode("Text");
                    text.appendChild(textEntry);

                    var root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
                    root.appendChild(text);

                    pngMeta.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);
                }
                var iioImage = new IIOImage(data, null, pngMeta);
                writer.write(null, iioImage, null);
            }
        }
    }
}
