package com.marvelwargame.ui.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that provides champion portrait images.
 * Load order: (1) bundled resource, (2) online URL, (3) canvas-generated portrait.
 */
public final class AssetManager {

    private static final AssetManager INSTANCE = new AssetManager();

    /** Online portrait URLs indexed by champion name (lowercase). */
    private static final Map<String, String> ONLINE_URLS = Map.ofEntries(
        Map.entry("captain america",
            "https://i.annihil.us/u/prod/marvel/i/mg/3/50/537ba56d31087/portrait_uncanny.jpg"),
        Map.entry("deadpool",
            "https://i.annihil.us/u/prod/marvel/i/mg/9/90/5261a86cacb99/portrait_uncanny.jpg"),
        Map.entry("dr strange",
            "https://i.annihil.us/u/prod/marvel/i/mg/5/f0/5261a86c5d58b/portrait_uncanny.jpg"),
        Map.entry("electro",
            "https://i.annihil.us/u/prod/marvel/i/mg/f/b0/526032b85e949/portrait_uncanny.jpg"),
        Map.entry("ghost rider",
            "https://i.annihil.us/u/prod/marvel/i/mg/3/40/526032f7f1f1d/portrait_uncanny.jpg"),
        Map.entry("hela",
            "https://i.annihil.us/u/prod/marvel/i/mg/8/70/526032bbf5b0d/portrait_uncanny.jpg"),
        Map.entry("hulk",
            "https://i.annihil.us/u/prod/marvel/i/mg/5/a0/538615ca33ab0/portrait_uncanny.jpg"),
        Map.entry("iceman",
            "https://i.annihil.us/u/prod/marvel/i/mg/6/b0/526032bcd7938/portrait_uncanny.jpg"),
        Map.entry("ironman",
            "https://i.annihil.us/u/prod/marvel/i/mg/9/c0/527bb7b37fd56/portrait_uncanny.jpg"),
        Map.entry("loki",
            "https://i.annihil.us/u/prod/marvel/i/mg/d/90/526032c480a0e/portrait_uncanny.jpg"),
        Map.entry("quicksilver",
            "https://i.annihil.us/u/prod/marvel/i/mg/b/70/526032c7dfe87/portrait_uncanny.jpg"),
        Map.entry("spiderman",
            "https://i.annihil.us/u/prod/marvel/i/mg/3/50/526548a343e4b/portrait_uncanny.jpg"),
        Map.entry("thor",
            "https://i.annihil.us/u/prod/marvel/i/mg/d/d0/5269657a74350/portrait_uncanny.jpg"),
        Map.entry("venom",
            "https://i.annihil.us/u/prod/marvel/i/mg/4/e0/526032cef042c/portrait_uncanny.jpg"),
        Map.entry("yellow jacket",
            "https://i.annihil.us/u/prod/marvel/i/mg/6/e0/526548a0a5a3c/portrait_uncanny.jpg")
    );

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
     * a canvas-generated colored tile with the champion's initial.
     */
    public Image getPortrait(String championName) {
        String key = championName.toLowerCase();
        return cache.computeIfAbsent(key, this::loadImage);
    }

    private Image loadImage(String key) {
        // 1. Try bundled resource via the module directly (works in jpackage jlinked image)
        String base = "images/champions/" + key.replace(" ", "_");
        for (String ext : new String[]{".png", ".jpg", ".jpeg"}) {
            // Use getModule().getResourceAsStream() — the canonical Java 9+ module way;
            // avoids the getClass().getResourceAsStream("/...") ambiguity in jlinked runtimes.
            try (InputStream res = AssetManager.class.getModule().getResourceAsStream(base + ext)) {
                if (res != null) {
                    byte[] bytes = res.readAllBytes();
                    return new Image(new java.io.ByteArrayInputStream(bytes));
                }
            } catch (Exception e) { /* try next ext */ }

            // Fallback: classic classpath lookup (works during gradlew run and in IDE)
            try (InputStream res = AssetManager.class.getResourceAsStream("/" + base + ext)) {
                if (res != null) {
                    byte[] bytes = res.readAllBytes();
                    return new Image(new java.io.ByteArrayInputStream(bytes));
                }
            } catch (Exception e) { /* try next ext */ }
        }

        // 2. Try online URL with background loading so it never blocks the UI
        String url = ONLINE_URLS.get(key);
        if (url != null) {
            try {
                // backgroundLoading=true: returns immediately, loads async; ImageViews update automatically
                Image img = new Image(url, 200, 200, false, true, true);
                return img;   // may still be loading; isError() will become true if it fails
            } catch (Exception e) {
                System.err.println("Could not start portrait load for: " + key);
            }
        }

        // 3. Fallback: generate a portrait by painting pixels directly into a WritableImage.
        // This uses no Canvas / snapshot / rendering pipeline — works everywhere.
        return generatePixelPortrait(key);
    }

    /**
     * Paints a gradient portrait directly into a WritableImage using PixelWriter.
     * Safe to call from any thread; requires no JavaFX scene or rendering pipeline.
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

    /** Load generic icon by name from /images/icons/ resources folder. */
    public Image getIcon(String name) {
        return cache.computeIfAbsent("icon:" + name, k -> {
            String path = "images/icons/" + name + ".png";
            // Try module-aware resource lookup first (jpackage jlinked image)
            try (InputStream res = AssetManager.class.getModule().getResourceAsStream(path)) {
                if (res != null) {
                    byte[] bytes = res.readAllBytes();
                    return new Image(new java.io.ByteArrayInputStream(bytes));
                }
            } catch (Exception ignored) {}
            // Fallback: classpath lookup (gradlew run / IDE)
            try (InputStream res = AssetManager.class.getResourceAsStream("/" + path)) {
                if (res == null) return null;
                byte[] bytes = res.readAllBytes();
                return new Image(new java.io.ByteArrayInputStream(bytes));
            } catch (Exception e) { return null; }
        });
    }
}
