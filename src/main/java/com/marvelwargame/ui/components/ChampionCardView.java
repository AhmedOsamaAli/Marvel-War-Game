package com.marvelwargame.ui.components;

import com.marvelwargame.model.world.*;
import com.marvelwargame.ui.util.AssetManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Reusable champion selection card with portrait, name, type badge, and stat preview.
 */
public class ChampionCardView extends VBox {

    private static final double CARD_W = 145;
    private static final double CARD_H = 210;
    private static final double PORTRAIT_H = 110;

    private final Champion champion;

    public ChampionCardView(Champion champion) {
        this.champion = champion;
        getStyleClass().add("champion-card");
        addTypeStyleClass();
        setPrefSize(CARD_W, CARD_H);
        setMaxSize(CARD_W, CARD_H);
        setSpacing(4);
        setAlignment(Pos.TOP_CENTER);

        // Portrait
        StackPane portrait = buildPortrait();
        getChildren().add(portrait);

        // Name
        Label nameLabel = new Label(champion.getName());
        nameLabel.getStyleClass().add("card-name");
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-alignment: center; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");
        getChildren().add(nameLabel);

        // Type badge
        Label badge = buildTypeBadge();
        getChildren().add(badge);

        // Stats
        HBox stats = buildStats();
        getChildren().add(stats);

        // Abilities summary
        Label abilitiesHint = new Label(champion.getAbilities().size() + " abilities");
        abilitiesHint.getStyleClass().add("dim-text");
        abilitiesHint.setStyle("-fx-font-size: 9px;");
        getChildren().add(abilitiesHint);

        // Tooltip with full stats
        Tooltip tt = new Tooltip(buildTooltipText());
        tt.setStyle("-fx-background-color: #0a0a1a; -fx-text-fill: white; "
                + "-fx-border-color: #ed1d24; -fx-border-width: 1; -fx-font-size: 11px;");
        Tooltip.install(this, tt);
    }

    public Champion getChampion() { return champion; }

    private StackPane buildPortrait() {
        StackPane pane = new StackPane();
        pane.setPrefSize(CARD_W, PORTRAIT_H);
        pane.setMaxSize(CARD_W, PORTRAIT_H);
        pane.setStyle("-fx-background-radius: 6 6 0 0; -fx-background-color: "
                + AssetManager.getInstance().getPlaceholderColor(champion.getName()) + ";");

        // Clip to rounded top corners
        Rectangle clip = new Rectangle(CARD_W, PORTRAIT_H);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        pane.setClip(clip);

        Image img = AssetManager.getInstance().getPortrait(champion.getName());
        if (img != null && !img.isError()) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(CARD_W);
            iv.setFitHeight(PORTRAIT_H);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            pane.getChildren().add(iv);
            img.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (Boolean.TRUE.equals(newVal)) Platform.runLater(() -> {
                    pane.getChildren().remove(iv);
                    pane.getChildren().add(buildFallbackPortrait());
                });
            });
        } else {
            pane.getChildren().add(buildFallbackPortrait());
        }
        return pane;
    }

    private StackPane buildFallbackPortrait() {
        StackPane fp = new StackPane();
        fp.setPrefSize(CARD_W, PORTRAIT_H);

        // Radial shimmer overlay
        Region tex = new Region();
        tex.setPrefSize(CARD_W, PORTRAIT_H); tex.setOpacity(0.18);
        tex.setStyle("-fx-background-color: radial-gradient(center 25% 25%, radius 75%,"
                + " rgba(255,255,255,0.3) 0%, transparent 70%);");
        tex.setMouseTransparent(true);

        // Large initial letter
        String initial = String.valueOf(champion.getName().charAt(0)).toUpperCase();
        Label letter = new Label(initial);
        letter.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; "
                + "-fx-text-fill: rgba(255,255,255,0.82); "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 10, 0.6, 0, 2);");
        letter.setTranslateY(-8);

        // Type chip bottom-right
        String tChar = (champion instanceof Hero) ? "HERO" : (champion instanceof Villain) ? "VILLAIN" : "ANTI-HERO";
        String tCol  = (champion instanceof Hero) ? "#2980b9" : (champion instanceof Villain) ? "#c0392b" : "#8e44ad";
        Label chip = new Label(tChar);
        chip.setStyle("-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: white;"
                + "-fx-background-color: " + tCol + "; -fx-padding: 2 5 2 5; -fx-background-radius: 3;");
        StackPane.setAlignment(chip, Pos.BOTTOM_RIGHT);
        chip.setTranslateX(-4); chip.setTranslateY(-4);

        fp.getChildren().addAll(tex, letter, chip);
        return fp;
    }

    private Label buildTypeBadge() {
        String type; String styleClass;
        if (champion instanceof Hero)     { type = "HERO";     styleClass = "badge-hero"; }
        else if (champion instanceof Villain) { type = "VILLAIN"; styleClass = "badge-villain"; }
        else                               { type = "ANTIHERO"; styleClass = "badge-antihero"; }
        Label badge = new Label(type);
        badge.getStyleClass().addAll("card-type-badge", styleClass);
        return badge;
    }

    private HBox buildStats() {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
            statLabel("\u2764 " + champion.getMaxHP(), "stat-hp"),
            statLabel("\u26A1 " + champion.getSpeed(), "stat-spd"),
            statLabel("\u2694 " + champion.getAttackDamage(), "stat-atk")
        );
        return row;
    }

    private Label statLabel(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        l.setStyle("-fx-font-size: 10px;");
        return l;
    }

    private void addTypeStyleClass() {
        if (champion instanceof Hero)     getStyleClass().add("hero-type");
        else if (champion instanceof Villain) getStyleClass().add("villain-type");
        else                               getStyleClass().add("antihero-type");
    }

    private String buildTooltipText() {
        String type = (champion instanceof Hero) ? "Hero" : (champion instanceof Villain) ? "Villain" : "Anti-Hero";
        String role = champRole(champion.getName());
        int    tier = champion.getMaxHP() >= 2000 ? 3 : champion.getMaxHP() >= 1400 ? 2 : 1;
        String tierStars = "\u2605".repeat(tier) + "\u2606".repeat(3 - tier);

        StringBuilder sb = new StringBuilder();
        sb.append("\u2694  ").append(champion.getName().toUpperCase()).append("  \u2694\n");
        sb.append(tierStars).append("  ").append(type.toUpperCase()).append("  \u2014  ").append(role).append("\n");
        sb.append("────────────────────────\n");
        sb.append("\u2764  HP      ").append(champion.getMaxHP())
          .append("   \u26A1  Speed   ").append(champion.getSpeed()).append("\n");
        sb.append("\u1F4A7  Mana    ").append(champion.getMana())
          .append("   \u2694  Attack  ").append(champion.getAttackDamage()).append("\n");
        sb.append("\uD83C\uDFAF  Range   ").append(champion.getAttackRange())
          .append("   \u26A1  Max AP  ").append(champion.getMaxActionPointsPerTurn()).append("\n");
        sb.append("────────────────────────\n");
        sb.append("ABILITIES\n");
        for (var a : champion.getAbilities()) {
            String aType = (a instanceof com.marvelwargame.model.abilities.DamagingAbility) ? "\uD83D\uDD25"
                         : (a instanceof com.marvelwargame.model.abilities.HealingAbility)  ? "\u2728" : "\u26D4";
            sb.append("  ").append(aType).append(" ").append(a.getName())
              .append("  [").append(a.getCastArea().toString().toLowerCase()).append("]")
              .append("  \u2212").append(a.getManaCost()).append(" mana\n");
        }
        return sb.toString().trim();
    }

    private String champRole(String name) {
        return switch (name.toLowerCase()) {
            case "captain america" -> "Frontline Tank";
            case "deadpool"        -> "Ranged Assassin";
            case "dr strange"      -> "Arcane Controller";
            case "electro"         -> "Storm Artillery";
            case "ghost rider"     -> "Hellfire Brawler";
            case "hela"            -> "Death Sovereign";
            case "hulk"            -> "Unstoppable Juggernaut";
            case "iceman"          -> "Cryo Sniper";
            case "ironman"         -> "Tech Marksman";
            case "loki"            -> "Shadow Trickster";
            case "quicksilver"     -> "Hyper Skirmisher";
            case "spiderman"       -> "Web Duelist";
            case "thor"            -> "Thunder Champion";
            case "venom"           -> "Symbiote Predator";
            case "yellow jacket"   -> "Nano Striker";
            default                -> "Champion";
        };
    }
}
