package com.marvelwargame.ui.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that provides champion portrait images.
 * Load order:
 *   1. Filesystem path relative to the exe (jpackage: images land in app/ dir)
 *   2. ClassLoader classpath lookup (gradlew run / IDE)
 *   3. Pixel-painted WritableImage fallback (always works)
 */
public final class AssetManager {

    private static final AssetManager INSTANCE = new AssetManager();

    /** Color palette for placeholders when images fail to load (hex strings). */
    private static final Map<String, String> PLACEHOLDER_COLORS = Map.ofEntries(
        Map.entry("captain america", "#1a4b8c"),
        Map.entry("deadpool",        "#c0392b"),
        Map.entry("dr strange",      "#6c3483"),
        Map.entry("electro",         "#f4d03f"),
        Map.entry("ghost rider",     "#e67e22"),
        Map.entry("hela",            "#1e8449"),
        Map.entry("hulk",            "#27ae60"),
        Map.entry("iceman",          "#85c1e9"),
        Map.entry("ironman",         "#922b21"),
        Map.entry("loki",            "#1a6b2b"),
        Map.entry("quicksilver",     "#717d7e"),
        Map.entry("spiderman",       "#c0392b"),
        Map.entry("thor",            "#2874a6"),
        Map.entry("venom",           "#1c2833"),
        Map.entry("yellow jacket",   "#d4ac0d")
    );

    private final Map<String, Image> cache = new ConcurrentHashMap<>();

    private AssetManager() {}

    public static AssetManager getInstance() { return INSTANCE; }

    /**
     * Returns the portrait Image for a champion. Never returns null – falls back to
     * a pixel-painted gradient tile.
     */
    public Image getPortrait(String championName) {
        String key = championName.toLowerCase();
        return cache.computeIfAbsent(key, this::loadImage);
    }

    /**
     * Finds the base directory that contains the images/ folder.
     *
     * Priority:
     * 1. System property "marvelwargame.resources" — set by Gradle for dev/run
     * 2. jpackage: locate app/ directory from java.class.path
     *
     * Returns null only if neither is available (should never happen).
     */
    private static Path detectResourcesDir() {
        // Dev / gradlew run: Gradle passes -Dmarvelwargame.resources=.../src/main/resources
        String devProp = System.getProperty("marvelwargame.resources");
        if (devProp != null) {
            Path p = Path.of(devProp);
            if (Files.isDirectory(p)) return p;
        }
        // jpackage: images are copied to the app/ dir alongside the JARs
        try {
            if (System.getProperty("jpackage.app-version") != null) {
                String cp = System.getProperty("java.class.path", "");
                for (String entry : cp.split(java.io.File.pathSeparator)) {
                    Path p = Path.of(entry);
                    if (Files.exists(p)) return p.getParent();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static final Path RESOURCES_DIR = detectResourcesDir();

    private Image loadImage(String key) {
        String base = "images/champions/" + key.replace(" ", "_");

        // Load directly from filesystem — bypasses JPMS module encapsulation entirely.
        if (RESOURCES_DIR != null) {
            for (String ext : new String[]{".png", ".jpg", ".jpeg"}) {
                Path file = RESOURCES_DIR.resolve(base + ext);
                if (Files.exists(file)) {
                    try { return new Image(file.toUri().toString()); }
                    catch (Exception ignored) {}
                }
            }
        }

        // Fallback: pixel-painted gradient — always works, no files needed
        return generatePixelPortrait(key);
    }

    /**
     * Paints a gradient portrait directly into a WritableImage using PixelWriter.
     */
    private Image generatePixelPortrait(String key) {
        try {
            Color base = Color.web(getPlaceholderColor(key));
            int W = 200, H = 250;
            WritableImage img = new WritableImage(W, H);
            PixelWriter pw = img.getPixelWriter();

            // Pre-compute top/bottom gradient stops
            Color top    = base.interpolate(Color.WHITE, 0.30);
            Color bottom = base.interpolate(Color.BLACK, 0.55);

            for (int y = 0; y < H; y++) {
                double t = (double) y / (H - 1);
                // Vertical gradient: top → bottom
                double r = top.getRed()   + t * (bottom.getRed()   - top.getRed());
                double g = top.getGreen() + t * (bottom.getGreen() - top.getGreen());
                double b = top.getBlue()  + t * (bottom.getBlue()  - top.getBlue());

                // Bottom-third darkening (cinematic shadow)
                double shadow = Math.max(0, (t - 0.6) / 0.4) * 0.50;

                for (int x = 0; x < W; x++) {
                    // Radial highlight from top-left quadrant
                    double dx = (double) x / W - 0.28;
                    double dy = (double) y / H - 0.18;
                    double dist = Math.sqrt(dx * dx + dy * dy) / 0.60;
                    double shine = Math.max(0, 0.26 * (1.0 - dist));

                    double fr = clamp(r * (1 - shadow) + shine * (1 - r));
                    double fg = clamp(g * (1 - shadow) + shine * (1 - g));
                    double fb = clamp(b * (1 - shadow) + shine * (1 - b));

                    pw.setArgb(x, y, toArgb(1.0, fr, fg, fb));
                }
            }

            // 2-pixel colored border
            Color border = base.brighter().brighter();
            for (int x = 0; x < W; x++) {
                pw.setArgb(x, 0,   toArgb(1, border.getRed(), border.getGreen(), border.getBlue()));
                pw.setArgb(x, 1,   toArgb(1, border.getRed(), border.getGreen(), border.getBlue()));
                pw.setArgb(x, H-2, toArgb(1, border.getRed(), border.getGreen(), border.getBlue()));
                pw.setArgb(x, H-1, toArgb(1, border.getRed(), border.getGreen(), border.getBlue()));
            }
            for (int y = 0; y < H; y++) {
                pw.setArgb(0,   y, toArgb(1, border.getRed(), border.getGreen(), border.getBlue()));
                pw.setArgb(1,   y, toArgb(1, border.getRed(), border.getGreen(), border.getBlue()));
                pw.setArgb(W-2, y, toArgb(1, border.getRed(), border.getGreen(), border.getBlue()));
                pw.setArgb(W-1, y, toArgb(1, border.getRed(), border.getGreen(), border.getBlue()));
            }

            return img;
        } catch (Exception e) {
            return null;
        }
    }

    private static double clamp(double v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }

    private static int toArgb(double a, double r, double g, double b) {
        return ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    /** Returns the hex color code associated with a champion for placeholder backgrounds. */
    public String getPlaceholderColor(String championName) {
        return PLACEHOLDER_COLORS.getOrDefault(championName.toLowerCase(), "#2c3e50");
    }

    /** Load generic icon by name from images/icons/ resources folder. */
    public Image getIcon(String name) {
        return cache.computeIfAbsent("icon:" + name, k -> {
            String path = "images/icons/" + name + ".png";
            if (RESOURCES_DIR != null) {
                Path file = RESOURCES_DIR.resolve(path);
                if (Files.exists(file)) {
                    try { return new Image(file.toUri().toString()); }
                    catch (Exception ignored) {}
                }
            }
            return null;
        });
    }
}
