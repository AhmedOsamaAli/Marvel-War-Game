package com.marvelwargame.ui.controllers;

import com.marvelwargame.MarvelWarApp;
import com.marvelwargame.engine.Game;
import com.marvelwargame.engine.Player;
import com.marvelwargame.engine.events.GameEvent;
import com.marvelwargame.exceptions.*;
import com.marvelwargame.model.abilities.*;
import com.marvelwargame.model.effects.*;
import com.marvelwargame.model.world.*;
import com.marvelwargame.ui.util.AssetManager;
import com.marvelwargame.ui.util.SoundManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoardController {

    // Top bar
    @FXML private Label lblP1Name, lblP2Name, lblTurnInfo, lblCurrentChampion;
    @FXML private HBox p1TeamStatus, p2TeamStatus;
    @FXML private ToggleButton btnMute;

    // Left panel
    @FXML private StackPane portraitPane;
    @FXML private Label lblChampName, lblChampType;
    @FXML private ProgressBar hpBar, manaBar;
    @FXML private Label lblHpValue, lblManaValue;
    @FXML private HBox apPips;
    @FXML private Label lblSpeed, lblAtk, lblRange;
    @FXML private FlowPane effectsPane;
    @FXML private Button btnLeader;

    // Center board
    @FXML private GridPane boardGrid;
    @FXML private Label lblP1BoardName, lblP2BoardName;
    @FXML private StackPane gameOverOverlay;
    @FXML private Label lblWinner;
    @FXML private StackPane turnBannerPane;
    @FXML private Label lblTurnBanner, lblTurnBannerSub;

    // Right panel - action tabs
    @FXML private ToggleButton tbMove, tbAttack, tbCast;
    @FXML private ToggleGroup actionModeGroup;
    @FXML private Button btnEndTurn;
    @FXML private Label lblActionHint;

    // Right panel - abilities
    @FXML private VBox abilitiesBox;
    @FXML private Button btnCastNow;

    // Bottom log
    @FXML private TextArea gameLog;

    // State
    private Game game;
    private int turnNumber = 1;
    private Ability selectedAbility = null;
    private Ability expandedAbility = null;  // which card's detail panel is open
    private StackPane[][] cells;
    private final Map<Champion, Integer> hpSnapshot = new HashMap<>();

    public void initGame(Player p1, Player p2) {
        try { game = new Game(p1, p2); }
        catch (UnallowedMovementException e) { appendLog("Error: " + e.getMessage()); return; }

        lblP1Name.setText(p1.getName().toUpperCase());
        lblP2Name.setText(p2.getName().toUpperCase());
        lblP1BoardName.setText("\u25b2 " + p1.getName().toUpperCase() + " \u25b2");
        lblP2BoardName.setText("\u25bc " + p2.getName().toUpperCase() + " \u25bc");

        subscribeToEvents();
        buildBoard();
        refreshAll();
        captureHPSnapshot();
        appendLog("=== Battle begins! Turn 1 ===");
        appendLog(game.getCurrentChampion().getName() + " acts first!");

        // Wire action-tab listeners for highlight updates
        tbMove.selectedProperty().addListener((obs, o, n) -> { if (n) { SoundManager.getInstance().play("click"); highlightCells(); } });
        tbAttack.selectedProperty().addListener((obs, o, n) -> { if (n) { SoundManager.getInstance().play("click"); highlightCells(); } });
        tbCast.selectedProperty().addListener((obs, o, n) -> { if (n) { SoundManager.getInstance().play("click"); highlightCells(); } });

        // Show first-turn banner
        showTurnBanner(game.getCurrentChampion());
    }

    @FXML public void initialize() { }

    // Event subscriptions
    private void subscribeToEvents() {
        game.getEventBus().subscribe(GameEvent.Type.CHAMPION_MOVED,        this::onMoved);
        game.getEventBus().subscribe(GameEvent.Type.CHAMPION_ATTACKED,     this::onAttacked);
        game.getEventBus().subscribe(GameEvent.Type.ABILITY_CAST,          this::onEvent);
        game.getEventBus().subscribe(GameEvent.Type.CHAMPION_DAMAGED,      this::onDamaged);
        game.getEventBus().subscribe(GameEvent.Type.CHAMPION_KNOCKED_OUT,  this::onKnockout);
        game.getEventBus().subscribe(GameEvent.Type.COVER_DESTROYED,       this::onEvent);
        game.getEventBus().subscribe(GameEvent.Type.LEADER_ABILITY_USED,   this::onLeaderUsed);
        game.getEventBus().subscribe(GameEvent.Type.GAME_OVER,             this::onGameOver);
        game.getEventBus().subscribe(GameEvent.Type.CHAMPION_TURN_STARTED, this::onTurnStarted);
    }

    private void onEvent(GameEvent e) {
        appendLog(e.message());
        refreshAll();
    }

    private void onMoved(GameEvent e) {
        SoundManager.getInstance().play("move");
        appendLog(e.message());
        refreshAll();
    }

    private void onAttacked(GameEvent e) {
        SoundManager.getInstance().play("attack");
        appendLog(e.message());
        refreshAll();
    }

    private void onDamaged(GameEvent e) {
        showDamageFloaters();
        SoundManager.getInstance().play("damage");
        appendLog(e.message());
        refreshAll();
        captureHPSnapshot();
    }

    private void onKnockout(GameEvent e) {
        SoundManager.getInstance().play("knockout");
        appendLog("\u2620 " + e.message());
        playShakeAnimation();
        refreshAll();
        captureHPSnapshot();
    }

    private void onLeaderUsed(GameEvent e) {
        SoundManager.getInstance().play("leader");
        appendLog("\u2605 LEADER ABILITY: " + e.message());
        refreshAll();
        captureHPSnapshot();
    }

    private void onTurnStarted(GameEvent e) {
        SoundManager.getInstance().play("endturn");
        appendLog(e.message());
        refreshAll();
        captureHPSnapshot();
        Platform.runLater(() -> showTurnBanner(game.getCurrentChampion()));
    }

    private void onGameOver(GameEvent e) {
        SoundManager.getInstance().play("gameover");
        appendLog("\u26a1 " + e.message());
        lblWinner.setText(e.message());
        gameOverOverlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(500), gameOverOverlay);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // Board build & render
    private void buildBoard() {
        boardGrid.getChildren().clear();
        cells = new StackPane[Game.BOARD_SIZE][Game.BOARD_SIZE];
        for (int row = Game.BOARD_SIZE - 1; row >= 0; row--) {
            for (int col = 0; col < Game.BOARD_SIZE; col++) {
                StackPane cell = new StackPane();
                cell.getStyleClass().add("board-cell");
                cell.setPrefSize(104, 104);
                cell.setMaxSize(104, 104);
                final int br = row, bc = col;
                cell.setOnMouseClicked(e -> onCellClicked(br, bc));
                cells[row][col] = cell;
                boardGrid.add(cell, col, Game.BOARD_SIZE - 1 - row);
            }
        }
        renderBoard();
    }

    private void renderBoard() {
        Object[][] board = game.getBoard();
        Champion current = game.getCurrentChampion();
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                StackPane cell = cells[r][c];
                // Keep any floating labels — remove only non-Label children (was vbox/etc.)
                cell.getChildren().removeIf(n -> !(n instanceof Label && n.getStyleClass().stream().anyMatch(s -> s.startsWith("floating"))));
                cell.getStyleClass().removeAll("occupied-p1", "occupied-p2", "cover", "empty",
                        "current-champion", "highlight-move", "highlight-attack", "highlight-ability");

                Object obj = board[r][c];
                if (obj == null) {
                    cell.getStyleClass().add("empty");
                } else if (obj instanceof Cover cover) {
                    cell.getStyleClass().add("cover");
                    cell.getChildren().add(buildCoverContent(cover));
                } else if (obj instanceof Champion champ) {
                    boolean isP1 = game.isFirstTeam(champ);
                    cell.getStyleClass().add(isP1 ? "occupied-p1" : "occupied-p2");
                    if (champ == current) cell.getStyleClass().add("current-champion");
                    cell.getChildren().add(buildChampionCellContent(champ, isP1));
                }
            }
        }
        highlightCells();
    }

    private VBox buildCoverContent(Cover cover) {
        VBox vb = new VBox(2); vb.setAlignment(Pos.CENTER); vb.setStyle("-fx-padding: 4;");

        double hpPct = Math.min(1.0, cover.getCurrentHP() / 1000.0);
        String iconName = hpPct < 0.3 ? "unlocked" : "locked";
        String iconColor = hpPct > 0.6 ? "#8bb8cc" : hpPct > 0.3 ? "#e8a44e" : "#e74c3c";

        // Icon with color tint
        Image img = AssetManager.getInstance().getIcon(iconName);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(32); iv.setFitHeight(32);
            javafx.scene.effect.ColorAdjust ca = new javafx.scene.effect.ColorAdjust();
            ca.setBrightness(hpPct < 0.3 ? -0.2 : 0.1);
            iv.setEffect(ca);
            vb.getChildren().add(iv);
        } else {
            Label ico = new Label("\uD83D\uDEE1");
            ico.setStyle("-fx-font-size: 22px;");
            vb.getChildren().add(ico);
        }

        // HP bar
        ProgressBar mini = new ProgressBar(hpPct);
        mini.setPrefWidth(56); mini.setPrefHeight(4);
        mini.getStyleClass().add(hpPct < 0.3 ? "hp-bar-low" : "hp-bar");
        vb.getChildren().add(mini);

        Label hp = new Label(cover.getCurrentHP() + " HP");
        hp.setStyle("-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: " + iconColor + ";");
        vb.getChildren().add(hp);
        return vb;
    }

    private VBox buildChampionCellContent(Champion champ, boolean isP1) {
        VBox vb = new VBox(2); vb.setAlignment(Pos.CENTER); vb.setStyle("-fx-padding: 3;");

        StackPane portrait = makePortraitNode(champ, 62, isP1);
        vb.getChildren().add(portrait);

        // Mini HP bar
        double hpPct = (double) champ.getCurrentHP() / champ.getMaxHP();
        ProgressBar mini = new ProgressBar(hpPct);
        mini.setPrefWidth(62); mini.setPrefHeight(4);
        mini.getStyleClass().add(hpPct < 0.3 ? "hp-bar-low" : "hp-bar");
        vb.getChildren().add(mini);

        Label name = new Label(shortName(champ.getName()));
        name.setStyle("-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: "
                + (isP1 ? "#85c1e9" : "#ff9090") + ";"
                + "-fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);");
        vb.getChildren().add(name);
        return vb;
    }

    /**
     * Builds a rich portrait StackPane for a champion.
     * isP1=true → blue player tint; isP1=false → red player tint.
     * isP1=null (use overload below) → neutral (for left-panel big portrait).
     */
    private StackPane makePortraitNode(Champion champ, int size, boolean isP1) {
        // Player accent colours
        String playerAccent  = isP1 ? "#1a6bbf" : "#bf1a1a";
        String playerLight   = isP1 ? "#3a8fe8" : "#e83a3a";
        String playerDark    = isP1 ? "#0a2a55" : "#550a0a";

        StackPane sp = new StackPane();
        sp.setPrefSize(size, size); sp.setMaxSize(size, size);

        // Rounded clip
        Rectangle clip = new Rectangle(size, size);
        clip.setArcWidth(size * 0.20); clip.setArcHeight(size * 0.20);
        sp.setClip(clip);

        String champCol = AssetManager.getInstance().getPlaceholderColor(champ.getName());

        // Background: player dark on top-left blending into champion colour on bottom-right
        sp.setStyle("-fx-background-color: linear-gradient(to bottom right, "
                + playerDark + " 0%, " + champCol + " 60%, " + darkenHex(champCol) + " 100%);");

        // Player-coloured top stripe (gives strong per-player identity even under real portrait)
        Region topStripe = new Region();
        topStripe.setPrefSize(size, (int)(size * 0.22));
        topStripe.setMaxSize(size, (int)(size * 0.22));
        topStripe.setStyle("-fx-background-color: linear-gradient(to bottom, "
                + playerAccent + "cc 0%, transparent 100%);");
        StackPane.setAlignment(topStripe, Pos.TOP_CENTER);
        topStripe.setMouseTransparent(true);

        // Radial shimmer
        Region shimmer = new Region();
        shimmer.setPrefSize(size, size); shimmer.setMouseTransparent(true); shimmer.setOpacity(0.15);
        shimmer.setStyle("-fx-background-color: radial-gradient(center 30% 30%, radius 70%,"
                + " rgba(255,255,255,0.40) 0%, transparent 70%);");

        // Large styled initial
        String initial = String.valueOf(champ.getName().charAt(0)).toUpperCase();
        Label letter = new Label(initial);
        letter.setStyle("-fx-font-size: " + (int)(size * 0.48) + "px; -fx-font-weight: bold; "
                + "-fx-text-fill: rgba(255,255,255,0.88); "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 8, 0.6, 0, 2);");
        letter.setTranslateY((int)(size * -0.04));

        sp.getChildren().addAll(topStripe, shimmer, letter);

        // Try real portrait (sits above background layers, below badges)
        Image img = AssetManager.getInstance().getPortrait(champ.getName());
        if (img != null && !img.isError()) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size); iv.setFitHeight(size); iv.setPreserveRatio(false);
            sp.getChildren().add(iv);
            img.errorProperty().addListener((obs, ov, nv) -> {
                if (Boolean.TRUE.equals(nv)) Platform.runLater(() -> sp.getChildren().remove(iv));
            });
            // Still show the top stripe over the real image for player identity
            Region imgStripe = new Region();
            imgStripe.setPrefSize(size, (int)(size * 0.22));
            imgStripe.setMaxSize(size, (int)(size * 0.22));
            imgStripe.setStyle("-fx-background-color: linear-gradient(to bottom, "
                    + playerAccent + "99 0%, transparent 100%);");
            StackPane.setAlignment(imgStripe, Pos.TOP_CENTER);
            imgStripe.setMouseTransparent(true);
            sp.getChildren().add(imgStripe);
        }

        // Player label chip — bottom-left corner
        if (size >= 40) {
            Label playerChip = new Label(isP1 ? "P1" : "P2");
            playerChip.setStyle("-fx-font-size: " + Math.max(7, size / 8) + "px; "
                    + "-fx-font-weight: bold; -fx-text-fill: white;"
                    + "-fx-background-color: " + playerAccent + "dd;"
                    + "-fx-padding: 1 3 1 3; -fx-background-radius: 3 3 0 0;");
            StackPane.setAlignment(playerChip, Pos.BOTTOM_LEFT);
            playerChip.setTranslateX(2); playerChip.setTranslateY(0);
            sp.getChildren().add(playerChip);
        }

        // Type badge — top-right corner
        String tChar = (champ instanceof Hero) ? "H" : (champ instanceof Villain) ? "V" : "A";
        String tCol  = (champ instanceof Hero) ? "#2980b9" : (champ instanceof Villain) ? "#c0392b" : "#8e44ad";
        Label typeBadge = new Label(tChar);
        typeBadge.setStyle("-fx-font-size: " + Math.max(7, size / 9) + "px; "
                + "-fx-font-weight: bold; -fx-text-fill: white;"
                + "-fx-background-color: " + tCol + "; -fx-padding: 1 3 1 3; -fx-background-radius: 3;");
        StackPane.setAlignment(typeBadge, Pos.TOP_RIGHT);
        typeBadge.setTranslateX(-2); typeBadge.setTranslateY(2);
        sp.getChildren().add(typeBadge);

        // Condition overlay
        if (champ.getCondition() != Condition.ACTIVE) {
            Region condBg = new Region();
            condBg.setPrefSize(size, size);
            condBg.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
            Label cond = new Label(conditionIcon(champ.getCondition()));
            cond.setStyle("-fx-font-size: " + (int)(size * 0.32) + "px;");
            StackPane condOverlay = new StackPane(condBg, cond);
            sp.getChildren().add(condOverlay);
        }

        return sp;
    }

    /** Overload for the left-panel big portrait — player is derived from game state. */
    private StackPane makePortraitNode(Champion champ, int size) {
        boolean isP1 = game != null && game.isFirstTeam(champ);
        return makePortraitNode(champ, size, isP1);
    }

    /** Lighten a hex color string by blending toward white. */
    private String lightenHex(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            r = Math.min(255, r + (255 - r) * 50 / 100);
            g = Math.min(255, g + (255 - g) * 50 / 100);
            b = Math.min(255, b + (255 - b) * 50 / 100);
            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) { return hex; }
    }

    /** Darken a hex color string. */
    private String darkenHex(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            r = r * 55 / 100; g = g * 55 / 100; b = b * 55 / 100;
            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) { return hex; }
    }

    // Left panel refresh
    private void refreshAll() {
        renderBoard();
        refreshChampionPanel();
        refreshAbilitiesPanel();
        refreshTeamStatusBar();
        refreshTurnInfo();
    }

    private void refreshChampionPanel() {
        Champion c = game.getCurrentChampion();
        if (c == null) return;

        // Large portrait in left panel
        portraitPane.getChildren().clear();
        StackPane bigPortrait = makePortraitNode(c, 158);
        // Remove corner clip so it fills the full pane edge-to-edge
        bigPortrait.setClip(null);
        bigPortrait.setPrefSize(158, 158); bigPortrait.setMaxSize(158, 158);
        portraitPane.getChildren().add(bigPortrait);

        lblChampName.setText(c.getName());
        lblChampType.setText(typeName(c));
        lblChampType.getStyleClass().removeAll("badge-hero", "badge-villain", "badge-antihero");
        lblChampType.getStyleClass().add(typeBadgeClass(c));

        double hpPct   = (double) c.getCurrentHP() / c.getMaxHP();
        double manaPct = Math.min(1.0, (double) c.getMana() / 1500.0);
        hpBar.setProgress(hpPct); manaBar.setProgress(manaPct);
        hpBar.getStyleClass().removeAll("hp-bar-low", "hp-bar");
        hpBar.getStyleClass().add(hpPct < 0.3 ? "hp-bar-low" : "hp-bar");
        lblHpValue.setText(c.getCurrentHP() + "/" + c.getMaxHP());
        lblManaValue.setText(c.getMana() + "");

        apPips.getChildren().clear();
        for (int i = 0; i < c.getMaxActionPointsPerTurn(); i++) {
            Circle pip = new Circle(5);
            pip.getStyleClass().add(i < c.getCurrentActionPoints() ? "ap-pip" : "ap-pip-empty");
            apPips.getChildren().add(pip);
        }

        lblSpeed.setText(String.valueOf(c.getSpeed()));
        lblAtk.setText(String.valueOf(c.getAttackDamage()));
        lblRange.setText(String.valueOf(c.getAttackRange()));

        effectsPane.getChildren().clear();
        for (Effect e : c.getAppliedEffects()) {
            String icon = effectIcon(e);
            String desc = effectDescription(e);
            Label badge = new Label(icon + " " + e.getName() + " \u00D7" + e.getDuration());
            badge.getStyleClass().addAll("effect-badge",
                    e.getType() == EffectType.BUFF ? "effect-buff" : "effect-debuff");
            Tooltip tt = new Tooltip(icon + "  " + e.getName().toUpperCase()
                    + "\n" + desc
                    + "\n\u23F3  " + e.getDuration() + " turn" + (e.getDuration() != 1 ? "s" : "") + " remaining");
            tt.setStyle("-fx-background-color: #070715; -fx-text-fill: white;"
                    + "-fx-border-color: " + (e.getType() == EffectType.BUFF ? "#27ae60" : "#e74c3c")
                    + "; -fx-border-width: 1; -fx-font-size: 11px;");
            Tooltip.install(badge, tt);
            effectsPane.getChildren().add(badge);
        }

        Player owner  = game.isFirstTeam(c) ? game.getFirstPlayer() : game.getSecondPlayer();
        boolean used  = game.isFirstTeam(c) ? game.isFirstLeaderUsed() : game.isSecondLeaderUsed();
        boolean isLdr = owner.getLeader() == c;
        btnLeader.setDisable(used || !isLdr);
        btnLeader.setOpacity(isLdr && !used ? 1.0 : 0.38);
        String leaderLabel;
        if (used) {
            leaderLabel = "\u2605 LEADER ABILITY (Used)";
        } else if (!isLdr) {
            leaderLabel = "LEADER ABILITY";
        } else if (c instanceof com.marvelwargame.model.world.Hero) {
            leaderLabel = "\u2605 CLEANSE + EMBRACE ALLIES";
        } else if (c instanceof com.marvelwargame.model.world.Villain) {
            leaderLabel = "\u2605 EXECUTE WEAKENED ENEMIES";
        } else {
            leaderLabel = "\u2605 DODGE — SHIELD WHOLE TEAM";
        }
        btnLeader.setText(leaderLabel);

        // Build descriptive tooltip for the leader ability button
        String leaderDesc;
        if (!isLdr) {
            leaderDesc = "Only the team leader can use this ability.\nCurrent leader: " + owner.getLeader().getName();
        } else if (used) {
            leaderDesc = "\u2605 LEADER ABILITY — Already used this battle.";
        } else if (c instanceof com.marvelwargame.model.world.Hero) {
            leaderDesc = "\u2605 HERO LEADER ABILITY\n\n"
                    + "Cleanses all negative effects from every ally,\n"
                    + "then grants Embrace to the entire team\n"
                    + "(reduces incoming damage for 2 turns).\n\n"
                    + "\u26A0 One-time use per battle.";
        } else if (c instanceof com.marvelwargame.model.world.Villain) {
            leaderDesc = "\u2605 VILLAIN LEADER ABILITY\n\n"
                    + "Ruthlessly executes any enemy below 30% HP\n"
                    + "(instantly sets their HP to 0 and knocks them out).\n\n"
                    + "\u26A0 One-time use per battle.";
        } else {
            leaderDesc = "\u2605 ANTI-HERO LEADER ABILITY\n\n"
                    + "Grants Dodge to every ally for 3 turns\n"
                    + "(the entire team evades all incoming attacks).\n\n"
                    + "\u26A0 One-time use per battle.";
        }
        Tooltip leaderTip = new Tooltip(leaderDesc);
        leaderTip.setWrapText(true);
        leaderTip.setMaxWidth(280);
        leaderTip.setStyle("-fx-background-color: #07071a; -fx-text-fill: #f0e070;"
                + " -fx-border-color: gold; -fx-border-width: 1; -fx-font-size: 12px; -fx-padding: 10;");
        Tooltip.install(btnLeader, leaderTip);
    }

    private void refreshAbilitiesPanel() {
        abilitiesBox.getChildren().clear();
        Champion c = game.getCurrentChampion();
        if (c == null) return;
        for (Ability a : c.getAbilities()) abilitiesBox.getChildren().add(buildAbilityCard(a, c));
    }

    private VBox buildAbilityCard(Ability a, Champion caster) {
        VBox card = new VBox(4);
        card.getStyleClass().add("ability-card");
        boolean canAfford = caster.getMana() >= a.getManaCost()
                && caster.getCurrentActionPoints() >= a.getRequiredActionPoints();
        boolean ready = a.isReady() && canAfford;
        if (a.onCooldown() || !canAfford) card.getStyleClass().add("on-cooldown");
        if (a == selectedAbility)         card.getStyleClass().add("selected");

        // Type: icon + label
        String typeLabel = (a instanceof DamagingAbility) ? "DAMAGE"
                         : (a instanceof HealingAbility)  ? "RESTORE" : "CONTROL";
        String typeClass = (a instanceof DamagingAbility) ? "ability-type-dmg"
                         : (a instanceof HealingAbility)  ? "ability-type-hel" : "ability-type-cc";
        String abilityIconName = (a instanceof DamagingAbility) ? "target"
                               : (a instanceof HealingAbility)  ? "plus" : "warning";
        String aoeDisplay = switch (a.getCastArea().toString()) {
            case "SINGLETARGET"  -> "Single";
            case "DIRECTIONAL"   -> "Line";
            case "SURROUND"      -> "Surround";
            case "TEAMTARGET"    -> "Team";
            case "SELFTARGET"    -> "Self";
            default              -> a.getCastArea().toString().toLowerCase();
        };

        HBox topRow = new HBox(5); topRow.setAlignment(Pos.CENTER_LEFT);
        Image abilityImg = AssetManager.getInstance().getIcon(abilityIconName);
        if (abilityImg != null) {
            ImageView aIv = new ImageView(abilityImg);
            aIv.setFitWidth(13); aIv.setFitHeight(13);
            topRow.getChildren().add(aIv);
        }
        Label typeL = new Label(typeLabel); typeL.getStyleClass().add(typeClass);
        Label aoeL  = new Label("\u25B8 " + aoeDisplay);
        aoeL.setStyle("-fx-font-size: 8px; -fx-text-fill: rgba(180,180,220,0.75);");
        topRow.getChildren().addAll(typeL, aoeL);

        Label nameL = new Label(a.getName()); nameL.getStyleClass().add("ability-name");

        // Cost + status row
        HBox costRow = new HBox(5); costRow.setAlignment(Pos.CENTER_LEFT);
        Label manaL = new Label("\uD83D\uDCA7 " + a.getManaCost());
        manaL.getStyleClass().add("ability-cost");
        Label apL = new Label("\u26A1 " + a.getRequiredActionPoints() + "AP");
        apL.getStyleClass().add("ability-cost");
        costRow.getChildren().addAll(manaL, apL);

        if (a.onCooldown()) {
            Label cdL = new Label("\u23F3 " + a.getCurrentCooldown() + "t");
            cdL.getStyleClass().add("ability-cooldown");
            costRow.getChildren().add(cdL);
        } else if (!canAfford) {
            Label noL = new Label(caster.getMana() < a.getManaCost() ? "\u274C Low mana" : "\u274C Low AP");
            noL.setStyle("-fx-font-size: 8px; -fx-text-fill: #e74c3c;");
            costRow.getChildren().add(noL);
        } else {
            Label readyL = new Label("\u2705 Ready \u2014 click to select");
            readyL.setStyle("-fx-font-size: 8px; -fx-text-fill: #58d68d;");
            costRow.getChildren().add(readyL);
        }

        // ── Inline detail panel (shown on click instead of hover tooltip) ──────
        VBox detailBox = new VBox(3);
        boolean isExpanded = (a == expandedAbility);
        detailBox.setVisible(isExpanded);
        detailBox.setManaged(isExpanded);
        detailBox.setStyle("-fx-padding: 4 0 0 0;");

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.25;");

        Label whatL;
        if (a instanceof DamagingAbility da) {
            whatL = new Label("\u2694 Deals " + da.getDamageAmount() + " damage");
            whatL.setStyle("-fx-font-size: 9px; -fx-text-fill: #ff9999;");
        } else if (a instanceof HealingAbility ha) {
            whatL = new Label("\u2764 Heals " + ha.getHealAmount() + " HP");
            whatL.setStyle("-fx-font-size: 9px; -fx-text-fill: #7fdf9f;");
        } else if (a instanceof CrowdControlAbility cc) {
            whatL = new Label("\uD83D\uDD17 Applies: " + cc.getEffect().getName());
            whatL.setStyle("-fx-font-size: 9px; -fx-text-fill: #c39bd3;");
        } else {
            whatL = new Label("Special ability");
            whatL.setStyle("-fx-font-size: 9px; -fx-text-fill: #c8c8e8;");
        }
        whatL.setWrapText(true);
        whatL.setMaxWidth(190);

        String aimDesc = switch (a.getCastArea().toString()) {
            case "SINGLETARGET" -> "\uD83C\uDFAF Click a highlighted enemy cell";
            case "DIRECTIONAL"  -> "\u2B06 Press a direction \u2014 fires a line";
            case "SURROUND"     -> "\uD83D\uDD50 Hits adjacent enemies \u2014 tap \u26A1 Cast Now";
            case "TEAMTARGET"   -> "\uD83D\uDC65 Affects all allies  \u2014 tap \u26A1 Cast Now";
            case "SELFTARGET"   -> "\uD83D\uDCCD Affects yourself    \u2014 tap \u26A1 Cast Now";
            default             -> a.getCastArea().toString();
        };
        Label aimL = new Label(aimDesc);
        aimL.setStyle("-fx-font-size: 9px; -fx-text-fill: #90b8d8;");
        aimL.setWrapText(true);
        aimL.setMaxWidth(190);

        detailBox.getChildren().addAll(sep, whatL, aimL);

        if (a.getCastRange() > 0) {
            Label rangeL = new Label("\uD83D\uDCCF Range: " + a.getCastRange() + " cells");
            rangeL.setStyle("-fx-font-size: 9px; -fx-text-fill: #90b8d8;");
            detailBox.getChildren().add(rangeL);
        }
        if (a.getBaseCooldown() > 0) {
            String cdText = "\u23F3 Cooldown: " + a.getBaseCooldown() + " turns";
            if (a.onCooldown()) cdText += "  (" + a.getCurrentCooldown() + " left)";
            Label cdInfoL = new Label(cdText);
            cdInfoL.setStyle("-fx-font-size: 9px; -fx-text-fill: #e09060;");
            detailBox.getChildren().add(cdInfoL);
        }

        card.getChildren().addAll(topRow, nameL, costRow, detailBox);

        // ── Single click handler for ALL cards (ready or not) ──────────────────
        card.setOnMouseClicked(e -> {
            boolean wasExpanded = (expandedAbility == a);
            if (ready) {
                SoundManager.getInstance().play("select");
                if (selectedAbility == a) {
                    selectedAbility = null;
                    expandedAbility = null;
                } else {
                    selectedAbility = a;
                    expandedAbility = a;
                    tbCast.setSelected(true);
                }
            } else {
                // Not ready: just toggle the info panel so player knows why
                expandedAbility = wasExpanded ? null : a;
            }
            refreshAbilitiesPanel();
            refreshTurnInfo();
            highlightCells();
        });

        return card;
    }

    private String buildAbilityTooltip(Ability a) {
        String typeHeader = (a instanceof DamagingAbility) ? "\uD83D\uDD25 DAMAGE ABILITY"
                          : (a instanceof HealingAbility)  ? "\u2728 HEALING ABILITY"
                                                           : "\u26D4 CROWD CONTROL";
        String aoeDesc = switch (a.getCastArea().toString()) {
            case "SINGLETARGET"  -> "Single target \u2014 click a cell";
            case "DIRECTIONAL"   -> "Line \u2014 press a direction";
            case "SURROUND"      -> "All adjacent enemies";
            case "TEAMTARGET"    -> "All allies";
            case "SELFTARGET"    -> "Self only";
            default              -> a.getCastArea().toString();
        };
        StringBuilder sb = new StringBuilder();
        sb.append(typeHeader).append("\n");
        sb.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n");
        sb.append("  ").append(a.getName().toUpperCase()).append("\n");
        sb.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n");
        if (a instanceof DamagingAbility da)
            sb.append("  \u2694  Damage    :  ").append(da.getDamageAmount()).append(" pts\n");
        else if (a instanceof HealingAbility ha)
            sb.append("  \u2764  Heal      :  ").append(ha.getHealAmount()).append(" pts\n");
        else if (a instanceof CrowdControlAbility cc)
            sb.append("  \uD83D\uDD17  Effect    :  ").append(cc.getEffect().getName()).append("\n");
        sb.append("  \uD83C\uDFAF  Target    :  ").append(aoeDesc).append("\n");
        if (a.getCastRange() > 0)
            sb.append("  \uD83D\uDCCF  Range     :  ").append(a.getCastRange()).append(" cells\n");
        sb.append("  \uD83D\uDCA7  Mana cost :  ").append(a.getManaCost()).append("\n");
        sb.append("  \u26A1  AP cost   :  ").append(a.getRequiredActionPoints()).append("\n");
        sb.append("  \u23F3  Cooldown  :  ");
        sb.append(a.getBaseCooldown() == 0 ? "None" : a.getBaseCooldown() + " turns");
        if (a.onCooldown()) sb.append("  \u00BB  ").append(a.getCurrentCooldown()).append(" remaining");
        return sb.toString();
    }

    private void refreshTeamStatusBar() {
        p1TeamStatus.getChildren().clear();
        p2TeamStatus.getChildren().clear();
        for (Champion c : game.getFirstPlayer().getTeam())  p1TeamStatus.getChildren().add(buildMiniDot(c, true));
        for (Champion c : game.getSecondPlayer().getTeam()) p2TeamStatus.getChildren().add(buildMiniDot(c, false));
    }

    private StackPane buildMiniDot(Champion c, boolean isP1) {
        boolean ko = c.getCondition() == Condition.KNOCKEDOUT;
        double pct = (double) c.getCurrentHP() / c.getMaxHP();

        // Mini portrait tile (22px) with player colour
        StackPane tile = makePortraitNode(c, 22, isP1);
        tile.setOpacity(ko ? 0.28 : 1.0);

        // HP ring — redraw as arc overlay only when low
        if (!ko && pct < 0.4) {
            Circle ring = new Circle(11);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.web("#e74c3c", 0.85));
            ring.setStrokeWidth(2.0);
            tile.getChildren().add(ring);
        }

        StackPane sp = new StackPane(tile);
        Tooltip tt = new Tooltip(c.getName() + "\nHP: " + c.getCurrentHP() + "/" + c.getMaxHP()
                + (ko ? "\n[KNOCKED OUT]" : ""));
        Tooltip.install(sp, tt);
        return sp;
    }

    private void refreshTurnInfo() {
        Champion c = game.getCurrentChampion();
        if (c == null) return;
        boolean isP1 = game.isFirstTeam(c);
        String pName = isP1 ? game.getFirstPlayer().getName() : game.getSecondPlayer().getName();
        lblTurnInfo.setText("TURN " + turnNumber + "  \u2014  " + pName.toUpperCase());
        lblCurrentChampion.setText(c.getName() + "'s Turn");
        lblCurrentChampion.setStyle("-fx-text-fill: " + (isP1 ? "#5dade2" : "#e05555") + "; -fx-font-size: 12px;");

        // Action hint
        if (lblActionHint != null) {
            if (tbMove.isSelected())   lblActionHint.setText("Press direction to move (1 AP)");
            else if (tbAttack.isSelected()) lblActionHint.setText("Press direction to attack (2 AP)");
            else if (selectedAbility != null) {
                AreaOfEffect aoe = selectedAbility.getCastArea();
                if (aoe == AreaOfEffect.SINGLETARGET)   lblActionHint.setText("Click a highlighted cell to target");
                else if (aoe == AreaOfEffect.DIRECTIONAL) lblActionHint.setText("Press direction to cast");
                else                                      lblActionHint.setText("Tap \u26A1 Cast Now below, or press any direction");
            } else lblActionHint.setText("Select an ability, then cast");
        }

        // Show/hide the Cast Now button for auto-cast abilities
        if (btnCastNow != null) {
            boolean showCastNow = selectedAbility != null
                    && selectedAbility.getCastArea() != AreaOfEffect.SINGLETARGET
                    && selectedAbility.getCastArea() != AreaOfEffect.DIRECTIONAL;
            btnCastNow.setVisible(showCastNow);
            btnCastNow.setManaged(showCastNow);
        }
    }

    // Cell highlighting
    private void highlightCells() {
        if (cells == null || game == null) return;
        clearHighlights();
        Champion c = game.getCurrentChampion();
        if (c == null || c.getCondition() == Condition.KNOCKEDOUT) return;
        int r = c.getLocation().x, col = c.getLocation().y;

        if (tbMove.isSelected()) {
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nr = r + d[0], nc = col + d[1];
                if (nr >= 0 && nr < 5 && nc >= 0 && nc < 5 && game.getBoard()[nr][nc] == null)
                    cells[nr][nc].getStyleClass().add("highlight-move");
            }
        } else if (tbAttack.isSelected()) {
            int range = c.getAttackRange();
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                for (int i = 1; i <= range; i++) {
                    int nr = r + d[0]*i, nc = col + d[1]*i;
                    if (nr < 0 || nr >= 5 || nc < 0 || nc >= 5) break;
                    cells[nr][nc].getStyleClass().add("highlight-attack");
                    if (game.getBoard()[nr][nc] != null) break;
                }
            }
        } else if (tbCast.isSelected() && selectedAbility != null) {
            AreaOfEffect aoe = selectedAbility.getCastArea();
            int castRange = selectedAbility.getCastRange();

            if (aoe == AreaOfEffect.SINGLETARGET) {
                // Highlight valid enemy targets; dim everything else
                for (int rr = 0; rr < 5; rr++) {
                    for (int cc = 0; cc < 5; cc++) {
                        if (game.getBoard()[rr][cc] instanceof Champion target
                                && target.getCondition() != Condition.KNOCKEDOUT
                                && game.isFirstTeam(c) != game.isFirstTeam(target)) {
                            int dist = Math.abs(r - rr) + Math.abs(col - cc);
                            if (dist <= castRange && dist > 0) {
                                cells[rr][cc].getStyleClass().add("highlight-ability");
                                continue;
                            }
                        }
                        // Dim all cells that are not valid targets (skip caster's own cell)
                        if (rr != r || cc != col)
                            cells[rr][cc].getStyleClass().add("cell-dimmed");
                    }
                }
            } else if (aoe == AreaOfEffect.SURROUND) {
                // Highlight adjacent cells that have enemies; dim all others
                int[][] adj = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
                for (int rr = 0; rr < 5; rr++) {
                    for (int cc = 0; cc < 5; cc++) {
                        if (rr == r && cc == col) continue;
                        boolean isAdj = Math.abs(rr - r) <= 1 && Math.abs(cc - col) <= 1;
                        boolean hasEnemy = game.getBoard()[rr][cc] instanceof Champion t
                                && t.getCondition() != Condition.KNOCKEDOUT
                                && game.isFirstTeam(c) != game.isFirstTeam(t);
                        if (isAdj && hasEnemy)
                            cells[rr][cc].getStyleClass().add("highlight-ability");
                        else
                            cells[rr][cc].getStyleClass().add("cell-dimmed");
                    }
                }
            } else if (aoe == AreaOfEffect.TEAMTARGET) {
                // Highlight same-team champions; dim all others
                for (int rr = 0; rr < 5; rr++) {
                    for (int cc = 0; cc < 5; cc++) {
                        if (rr == r && cc == col) continue;
                        if (game.getBoard()[rr][cc] instanceof Champion ally
                                && ally.getCondition() != Condition.KNOCKEDOUT
                                && game.isFirstTeam(c) == game.isFirstTeam(ally)) {
                            cells[rr][cc].getStyleClass().add("highlight-team");
                        } else {
                            cells[rr][cc].getStyleClass().add("cell-dimmed");
                        }
                    }
                }
            } else if (aoe == AreaOfEffect.SELFTARGET) {
                // Highlight the caster's own cell; dim everything else
                cells[r][col].getStyleClass().add("highlight-self");
                for (int rr = 0; rr < 5; rr++)
                    for (int cc = 0; cc < 5; cc++)
                        if (rr != r || cc != col)
                            cells[rr][cc].getStyleClass().add("cell-dimmed");
            } else if (aoe == AreaOfEffect.DIRECTIONAL) {
                // Dim all cells — direction buttons control targeting
                for (int rr = 0; rr < 5; rr++)
                    for (int cc = 0; cc < 5; cc++)
                        if (rr != r || cc != col)
                            cells[rr][cc].getStyleClass().add("cell-dimmed");
            }
        }
    }

    private void clearHighlights() {
        if (cells == null) return;
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                cells[r][c].getStyleClass().removeAll(
                    "highlight-move", "highlight-attack", "highlight-ability",
                    "highlight-self", "highlight-team", "cell-dimmed");
    }

    // Floating damage numbers
    private void captureHPSnapshot() {
        hpSnapshot.clear();
        Object[][] board = game.getBoard();
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                if (board[r][c] instanceof Champion ch) hpSnapshot.put(ch, ch.getCurrentHP());
    }

    private void showDamageFloaters() {
        Object[][] board = game.getBoard();
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (board[r][c] instanceof Champion ch) {
                    Integer prev = hpSnapshot.get(ch);
                    if (prev != null && prev != ch.getCurrentHP()) {
                        int diff = prev - ch.getCurrentHP();
                        showFloatingText(r, c, diff > 0 ? "-" + diff : "+" + (-diff), diff > 0);
                    }
                }
            }
        }
    }

    private void showFloatingText(int boardRow, int boardCol, String text, boolean isDamage) {
        StackPane cell = cells[boardRow][boardCol];
        Label lbl = new Label(text);
        lbl.getStyleClass().add(isDamage ? "floating-dmg" : "floating-heal");
        lbl.setMouseTransparent(true);
        cell.getChildren().add(lbl);

        TranslateTransition tt = new TranslateTransition(Duration.millis(900), lbl);
        tt.setByY(-55);
        FadeTransition ft = new FadeTransition(Duration.millis(900), lbl);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setDelay(Duration.millis(250));
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.setOnFinished(e -> cell.getChildren().remove(lbl));
        pt.play();
    }

    // Turn announcement banner
    private void showTurnBanner(Champion c) {
        if (turnBannerPane == null || c == null) return;
        boolean isP1  = game.isFirstTeam(c);
        String typeColor = (c instanceof Hero) ? "#4a90e2" : (c instanceof Villain) ? "#e23636" : "#9b59b6";

        lblTurnBanner.setText(c.getName().toUpperCase() + "'s TURN");
        lblTurnBanner.setStyle("-fx-text-fill: " + typeColor + ";");
        if (lblTurnBannerSub != null) {
            lblTurnBannerSub.setText((isP1 ? game.getFirstPlayer().getName()
                                           : game.getSecondPlayer().getName()).toUpperCase()
                    + " \u2022 " + typeName(c));
        }

        turnBannerPane.setVisible(true);
        turnBannerPane.setOpacity(0);
        turnBannerPane.setTranslateY(-40);
        turnBannerPane.maxHeightProperty().set(56);

        FadeTransition fi = new FadeTransition(Duration.millis(250), turnBannerPane);
        fi.setToValue(1);
        TranslateTransition ti = new TranslateTransition(Duration.millis(250), turnBannerPane);
        ti.setToY(0);
        ParallelTransition show = new ParallelTransition(fi, ti);

        PauseTransition hold = new PauseTransition(Duration.seconds(1.6));

        FadeTransition fo = new FadeTransition(Duration.millis(300), turnBannerPane);
        fo.setToValue(0);
        fo.setOnFinished(ev -> { turnBannerPane.setVisible(false); turnBannerPane.setTranslateY(0); });

        new SequentialTransition(show, hold, fo).play();
    }

    // Action handlers
    @FXML private void onDirectionUp()    { executeDirection(Direction.UP); }
    @FXML private void onDirectionDown()  { executeDirection(Direction.DOWN); }
    @FXML private void onDirectionLeft()  { executeDirection(Direction.LEFT); }
    @FXML private void onDirectionRight() { executeDirection(Direction.RIGHT); }

    private void executeDirection(Direction d) {
        if (tbMove.isSelected()) {
            try { game.move(d); }
            catch (Exception e) { SoundManager.getInstance().play("error"); appendLog("\u26a0 " + e.getMessage()); }
        } else if (tbAttack.isSelected()) {
            try { game.attack(d); }
            catch (Exception e) { SoundManager.getInstance().play("error"); appendLog("\u26a0 " + e.getMessage()); }
        } else if (tbCast.isSelected() && selectedAbility != null) {
            AreaOfEffect aoe = selectedAbility.getCastArea();
            if (aoe == AreaOfEffect.DIRECTIONAL) {
                try { game.castAbility(selectedAbility, d); selectedAbility = null; expandedAbility = null; refreshAll(); }
                catch (Exception e) { SoundManager.getInstance().play("error"); appendLog("\u26a0 " + e.getMessage()); }
            } else if (aoe != AreaOfEffect.SINGLETARGET) {
                try { game.castAbility(selectedAbility); selectedAbility = null; expandedAbility = null; refreshAll(); }
                catch (Exception e) { SoundManager.getInstance().play("error"); appendLog("\u26a0 " + e.getMessage()); }
            } else {
                appendLog("Click a highlighted cell to target this ability.");
            }
        }
    }

    private void onCellClicked(int row, int col) {
        if (!tbCast.isSelected() || selectedAbility == null) return;
        if (selectedAbility.getCastArea() == AreaOfEffect.SINGLETARGET) {
            try { game.castAbility(selectedAbility, row, col); selectedAbility = null; expandedAbility = null; refreshAll(); }
            catch (Exception e) { SoundManager.getInstance().play("error"); appendLog("\u26a0 " + e.getMessage()); }
        }
    }

    @FXML private void onCastNow() {
        // Fires auto-cast abilities (SURROUND, TEAMTARGET, SELFTARGET) without needing direction
        if (!tbCast.isSelected() || selectedAbility == null) return;
        AreaOfEffect aoe = selectedAbility.getCastArea();
        if (aoe != AreaOfEffect.SINGLETARGET && aoe != AreaOfEffect.DIRECTIONAL) {
            try {
                game.castAbility(selectedAbility);
                selectedAbility = null;
                expandedAbility = null;
                refreshAll();
            } catch (Exception e) {
                SoundManager.getInstance().play("error");
                appendLog("\u26a0 " + e.getMessage());
            }
        }
    }

    @FXML private void onEndTurn() {
        SoundManager.getInstance().play("endturn");
        game.endTurn();
        turnNumber++;
        selectedAbility = null;
        expandedAbility = null;
    }

    @FXML private void onLeaderAbility() {
        try { game.useLeaderAbility(); }
        catch (Exception e) { SoundManager.getInstance().play("error"); appendLog("\u26a0 " + e.getMessage()); }
    }

    @FXML private void onMuteToggle() {
        boolean nowMuted = !SoundManager.getInstance().isMuted();
        SoundManager.getInstance().setMuted(nowMuted);
        if (btnMute != null) {
            Image icon = AssetManager.getInstance().getIcon(nowMuted ? "audioOff" : "audioOn");
            if (icon != null) {
                ImageView iv = new ImageView(icon);
                iv.setFitWidth(18); iv.setFitHeight(18);
                btnMute.setGraphic(iv);
                btnMute.setText("");
            } else {
                btnMute.setText(nowMuted ? "MUTE" : "SND");
            }
        }
    }

    @FXML private void onPlayAgain() {
        try { MarvelWarApp.navigateTo("PlayerSetup"); }
        catch (Exception e) { appendLog("\u26a0 " + e.getMessage()); }
    }

    @FXML private void onMainMenu() {
        try { MarvelWarApp.navigateTo("MainMenu"); }
        catch (Exception e) { appendLog("\u26a0 " + e.getMessage()); }
    }

    // Helpers
    private void appendLog(String msg) {
        Platform.runLater(() -> gameLog.appendText(msg + "\n"));
    }

    private void playShakeAnimation() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(55), boardGrid);
        shake.setByX(7); shake.setAutoReverse(true); shake.setCycleCount(5); shake.play();
    }

    private String typeName(Champion c) {
        if (c instanceof Hero)    return "HERO";
        if (c instanceof Villain) return "VILLAIN";
        return "ANTI-HERO";
    }
    private String typeBadgeClass(Champion c) {
        if (c instanceof Hero)    return "badge-hero";
        if (c instanceof Villain) return "badge-villain";
        return "badge-antihero";
    }
    private String shortName(String name) { return name.length() > 9 ? name.substring(0, 8) + "\u2026" : name; }
    private String conditionIcon(Condition c) {
        return switch (c) {
            case INACTIVE   -> "\uD83D\uDE34";
            case ROOTED     -> "\u26D3";
            case KNOCKEDOUT -> "\u2620";
            default         -> "";
        };
    }

    private String effectIcon(Effect e) {
        String name = e.getClass().getSimpleName().toLowerCase();
        return switch (name) {
            case "embrace"    -> "\uD83D\uDC9E";
            case "dodge"      -> "\uD83C\uDFC3";
            case "shield"     -> "\uD83D\uDEE1";
            case "speedup"    -> "\u26A1";
            case "powerup"    -> "\uD83D\uDD25";
            case "shock"      -> "\uD83C\uDF29";
            case "root"       -> "\uD83C\uDF3F";
            case "stun"       -> "\uD83D\uDCAB";
            case "silence"    -> "\uD83D\uDD07";
            case "disarm"     -> "\uD83D\uDD2B";
            default           -> "\u2B50";
        };
    }

    private String effectDescription(Effect e) {
        String name = e.getClass().getSimpleName().toLowerCase();
        return switch (name) {
            case "embrace"  -> "All debuffs cleansed. Damage reduced this turn.";
            case "dodge"    -> "50% chance to evade incoming attacks.";
            case "shield"   -> "Absorbs the next hit completely.";
            case "speedup"  -> "Movement and AP boosted.";
            case "powerup"  -> "Attack damage significantly increased.";
            case "shock"    -> "Stats reduced \u2014 weakened state.";
            case "root"     -> "Cannot move \u2014 feet bound to the ground.";
            case "stun"     -> "Stunned \u2014 cannot act this turn.";
            case "silence"  -> "Abilities are sealed and cannot be cast.";
            case "disarm"   -> "Weapon seized \u2014 cannot attack.";
            default         -> "Status effect active.";
        };
    }
}
