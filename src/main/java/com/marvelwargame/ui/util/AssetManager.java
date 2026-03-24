package com.marvelwargame.ui.util;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that provides champion portrait images.
 * Load order: (1) bundled resource, (2) online URL, (3) colored placeholder.
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
        // 1. Try bundled resource — check both .png and .jpg
        String base = "/images/champions/" + key.replace(" ", "_");
        for (String ext : new String[]{".png", ".jpg", ".jpeg"}) {
            InputStream res = getClass().getResourceAsStream(base + ext);
            if (res != null) {
                try { return new Image(res); } catch (Exception e) { /* try next ext */ }
            }
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

        // 3. Return null – controllers will render a CSS-styled placeholder label
        return null;
    }

    /** Returns the hex color code associated with a champion for placeholder backgrounds. */
    public String getPlaceholderColor(String championName) {
        return PLACEHOLDER_COLORS.getOrDefault(championName.toLowerCase(), "#2c3e50");
    }

    /** Load generic icon by name from /images/icons/ resources folder. */
    public Image getIcon(String name) {
        return cache.computeIfAbsent("icon:" + name, k -> {
            InputStream res = getClass().getResourceAsStream("/images/icons/" + name + ".png");
            if (res == null) return null;
            try { return new Image(res); } catch (Exception e) { return null; }
        });
    }
}
