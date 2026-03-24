package com.marvelwargame.ui.controllers;

import com.marvelwargame.MarvelWarApp;
import com.marvelwargame.engine.Player;
import com.marvelwargame.model.abilities.*;
import com.marvelwargame.model.effects.Effect;
import com.marvelwargame.model.world.*;
import com.marvelwargame.ui.components.ChampionCardView;
import com.marvelwargame.ui.util.AssetManager;
import com.marvelwargame.ui.util.SoundManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Champion selection screen.
 * Phase 1: Player 1 picks 3 champions + leader.
 * Phase 2: Player 2 picks 3 champions + leader.
 */
public class ChampionSelectController {

    @FXML private Label lblInstruction;
    @FXML private Label lblP1Progress;
    @FXML private Label lblP2Progress;
    @FXML private FlowPane championGrid;
    @FXML private HBox selectedChampionsBox;
    @FXML private Label lblCurrentPlayer;
    @FXML private Label lblLeaderHint;
    @FXML private Button btnConfirm;
    @FXML private ToggleButton tbFilterAll, tbFilterHeroes, tbFilterVillains, tbFilterAntiHeroes;
    @FXML private ToggleGroup filterGroup;
    @FXML private VBox profilePanel;

    private String currentFilter = "ALL";

    private Player player1, player2;
    private Player currentPlayer;
    private boolean selectingPlayer2 = false;

    private final List<Champion> p1Selected = new ArrayList<>();
    private final List<Champion> p2Selected = new ArrayList<>();
    private Champion p1Leader = null, p2Leader = null;

    private final List<ChampionCardView> cardViews = new ArrayList<>();

    /** Called by PlayerSetupController after navigation. */
    public void initForPlayer(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;
        buildGrid();
        refreshUI();
        SoundManager.getInstance().play("confirm");
    }

    @FXML public void initialize() { /* grid built in initForPlayer */ }

    // ── Filter tabs ────────────────────────────────────────────────────────────
    @FXML private void onFilterAll()       { setFilter("ALL"); }
    @FXML private void onFilterHeroes()    { setFilter("HEROES"); }
    @FXML private void onFilterVillains()  { setFilter("VILLAINS"); }
    @FXML private void onFilterAntiHeroes(){ setFilter("ANTI-HEROES"); }

    private void setFilter(String f) {
        currentFilter = f;
        SoundManager.getInstance().play("click");
        applyFilter();
    }

    private void applyFilter() {
        championGrid.getChildren().clear();
        for (ChampionCardView card : cardViews) {
            Champion c = card.getChampion();
            boolean show = switch (currentFilter) {
                case "HEROES"       -> c instanceof Hero;
                case "VILLAINS"     -> c instanceof Villain;
                case "ANTI-HEROES"  -> c instanceof AntiHero;
                default             -> true;
            };
            if (show) {
                championGrid.getChildren().add(card);
                FadeTransition ft = new FadeTransition(Duration.millis(220), card);
                ft.setFromValue(0); ft.setToValue(1); ft.play();
            }
        }
        refreshCardStyles();
    }

    // ── Build champion selection grid ──────────────────────────────────────────

    private void buildGrid() {
        championGrid.getChildren().clear();
        cardViews.clear();
        for (Champion c : MarvelWarApp.getAvailableChampions()) {
            ChampionCardView card = new ChampionCardView(c);
            card.setOnMouseClicked(e -> onChampionClicked(c, card));
            card.setOnMouseEntered(e -> showProfile(c));
            cardViews.add(card);
            championGrid.getChildren().add(card);
        }
    }

    private void onChampionClicked(Champion champion, ChampionCardView card) {
        List<Champion> selected = selectingPlayer2 ? p2Selected : p1Selected;

        if (selected.contains(champion)) {
            SoundManager.getInstance().play("select");
            // Toggle leader
            if (selectingPlayer2) { p2Leader = (p2Leader == champion) ? null : champion; }
            else                  { p1Leader = (p1Leader == champion) ? null : champion; }
            refreshSelectedBar();
            refreshCardStyles();
            return;
        }

        List<Champion> other = selectingPlayer2 ? p1Selected : p2Selected;
        if (other.contains(champion)) return;

        if (selected.size() < 3) {
            SoundManager.getInstance().play("select");
            selected.add(champion);
            if (selected.size() == 1) {
                if (selectingPlayer2) p2Leader = champion;
                else p1Leader = champion;
            }
        }
        refreshUI();
    }

    private void refreshUI() {
        List<Champion> selected = selectingPlayer2 ? p2Selected : p1Selected;
        int count = selected.size();

        lblP1Progress.setText(player1.getName() + ": " + p1Selected.size() + "/3");
        lblP2Progress.setText(player2.getName() + ": " + p2Selected.size() + "/3");
        lblCurrentPlayer.setText((selectingPlayer2 ? player2.getName() : player1.getName())
                .toUpperCase() + " SELECTION");
        lblInstruction.setText((selectingPlayer2 ? player2.getName() : player1.getName())
                + " — Select 3 Champions  (click a selected champion to set as leader)");
        lblLeaderHint.setText("Current leader: "
                + (selectingPlayer2 ? (p2Leader != null ? p2Leader.getName() : "none")
                                    : (p1Leader != null ? p1Leader.getName() : "none")));

        btnConfirm.setDisable(count < 3);
        if (count >= 3) btnConfirm.setText(selectingPlayer2 ? "START BATTLE  ▶" : "PLAYER 2 TURN  ▶");

        refreshSelectedBar();
        refreshCardStyles();
    }

    private void refreshSelectedBar() {
        selectedChampionsBox.getChildren().clear();
        List<Champion> selected = selectingPlayer2 ? p2Selected : p1Selected;
        Champion leader = selectingPlayer2 ? p2Leader : p1Leader;
        for (Champion c : selected) {
            StackPane mini = buildMiniCard(c, c == leader);
            selectedChampionsBox.getChildren().add(mini);
        }
    }

    private StackPane buildMiniCard(Champion c, boolean isLeader) {
        StackPane pane = new StackPane();
        String typeColor = (c instanceof Hero) ? "rgba(74,144,226,0.7)"
                         : (c instanceof Villain) ? "rgba(226,54,54,0.7)"
                         : "rgba(155,89,182,0.7)";
        pane.setStyle("-fx-background-color: " + AssetManager.getInstance().getPlaceholderColor(c.getName())
                + "; -fx-background-radius: 8;"
                + "-fx-border-color: " + (isLeader ? "gold" : typeColor) + ";"
                + "-fx-border-width: " + (isLeader ? "2.5" : "1") + ";"
                + "-fx-border-radius: 8;"
                + (isLeader ? "-fx-effect: dropshadow(gaussian,rgba(255,215,0,0.8),14,0.5,0,0);" : ""));
        pane.setPrefSize(90, 58);
        VBox vb = new VBox(2); vb.setAlignment(javafx.geometry.Pos.CENTER);
        Label name = new Label(c.getName().length() > 10 ? c.getName().substring(0, 9) + "\u2026" : c.getName());
        name.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label star = new Label(isLeader ? "\u2605 LEADER" : typeName(c));
        star.setStyle("-fx-font-size: 8px; -fx-text-fill: " + (isLeader ? "gold" : "rgba(200,200,220,0.7)") + ";");
        vb.getChildren().addAll(name, star);
        pane.getChildren().add(vb);
        return pane;
    }

    private String typeName(Champion c) {
        if (c instanceof Hero)    return "HERO";
        if (c instanceof Villain) return "VILLAIN";
        return "ANTI-HERO";
    }

    private void refreshCardStyles() {
        for (ChampionCardView card : cardViews) {
            Champion c = card.getChampion();
            boolean inP1 = p1Selected.contains(c);
            boolean inP2 = p2Selected.contains(c);
            boolean activeLeader = (c == p1Leader && !selectingPlayer2)
                                || (c == p2Leader && selectingPlayer2);
            boolean currentSel = selectingPlayer2 ? inP2 : inP1;
            boolean otherSel   = selectingPlayer2 ? inP1 : inP2;

            card.getStyleClass().removeAll("selected", "leader");
            card.setOpacity(otherSel ? 0.3 : 1.0);
            if (otherSel) return;
            if (currentSel) card.getStyleClass().add("selected");
            if (activeLeader && currentSel) card.getStyleClass().add("leader");
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    @FXML
    private void onBack() {
        SoundManager.getInstance().play("back");
        if (selectingPlayer2) {
            selectingPlayer2 = false;
            p2Selected.clear(); p2Leader = null;
            refreshUI();
        } else {
            try { MarvelWarApp.navigateTo("PlayerSetup"); }
            catch (Exception e) { printError(e); }
        }
    }

    @FXML
    private void onConfirm() {
        SoundManager.getInstance().play("confirm");
        if (!selectingPlayer2) {
            selectingPlayer2 = true;
            currentFilter = "ALL";
            if (tbFilterAll != null) tbFilterAll.setSelected(true);
            refreshUI();
            applyFilter();
        } else {
            applyTeams();
            try {
                MarvelWarApp.navigateWithController("GameBoard", ctrl ->
                        ((GameBoardController) ctrl).initGame(player1, player2));
            } catch (Exception e) { printError(e); }
        }
    }

    private static void printError(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) cause = cause.getCause();
        System.err.println("[ERROR] Navigation failed: " + cause);
        cause.printStackTrace(System.err);
    }

    // ── Champion Profile Panel ───────────────────────────────────────────────

    private void showProfile(Champion c) {
        profilePanel.getChildren().clear();

        // Portrait
        StackPane portrait = new StackPane();
        portrait.setPrefSize(295, 190);
        portrait.setMaxHeight(190);
        portrait.setStyle("-fx-background-color: " + AssetManager.getInstance().getPlaceholderColor(c.getName()) + ";");

        Image img = AssetManager.getInstance().getPortrait(c.getName());
        if (img != null && !img.isError()) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(295); iv.setFitHeight(190);
            iv.setPreserveRatio(false); iv.setSmooth(true);
            portrait.getChildren().add(iv);
            img.errorProperty().addListener((obs, ov, nv) -> {
                if (Boolean.TRUE.equals(nv)) Platform.runLater(() -> portrait.getChildren().remove(iv));
            });
        }
        String chipBg = (c instanceof Hero) ? "#1a6bbf" : (c instanceof Villain) ? "#9b1a1a" : "#6b1abf";
        Label typeChip = new Label(typeName(c));
        typeChip.setStyle("-fx-background-color: " + chipBg + "; -fx-text-fill: white; "
                + "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 3 8 3 8;");
        StackPane.setAlignment(typeChip, Pos.BOTTOM_LEFT);
        portrait.getChildren().add(typeChip);
        profilePanel.getChildren().add(portrait);

        // Name
        Label name = new Label(c.getName().toUpperCase());
        name.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f0f0ff;"
                + " -fx-padding: 10 14 2 14; -fx-wrap-text: true;");
        name.setMaxWidth(295);
        profilePanel.getChildren().add(name);

        // Stats grid
        GridPane stats = new GridPane();
        stats.setHgap(6); stats.setVgap(5);
        stats.setStyle("-fx-padding: 4 14 8 14;");
        addStat(stats, 0, "\u2764", "HP",      String.valueOf(c.getMaxHP()));
        addStat(stats, 1, "\u26A1", "Speed",   String.valueOf(c.getSpeed()));
        addStat(stats, 2, "\u2694", "Attack",  c.getAttackDamage() + " dmg");
        addStat(stats, 3, "\uD83C\uDFAF", "Range", c.getAttackRange() + " cells");
        addStat(stats, 4, "\uD83D\uDCA7", "Mana",  String.valueOf(c.getMana()));
        profilePanel.getChildren().add(stats);

        profilePanel.getChildren().add(profileDivider());

        // Abilities header
        Label abHeader = new Label("ABILITIES");
        abHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-text-fill: rgba(200,200,220,0.5); -fx-padding: 6 14 4 14; -fx-letter-spacing: 2;");
        profilePanel.getChildren().add(abHeader);

        for (Ability a : c.getAbilities()) {
            profilePanel.getChildren().add(buildProfileAbilityCard(a));
        }

        profilePanel.getChildren().add(profileDivider());

        // Leader ability
        Label leaderHeader = new Label("\u2605 LEADER ABILITY");
        leaderHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-text-fill: gold; -fx-padding: 6 14 4 14;");
        profilePanel.getChildren().add(leaderHeader);

        Label leaderDesc = new Label(leaderAbilityDesc(c));
        leaderDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(220,210,170,0.9);"
                + " -fx-wrap-text: true; -fx-padding: 0 14 14 14;");
        leaderDesc.setMaxWidth(295);
        profilePanel.getChildren().add(leaderDesc);
    }

    private void addStat(GridPane grid, int row, String icon, String label, String value) {
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 11px; -fx-min-width: 20;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(180,180,200,0.55); -fx-min-width: 55;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #e0e0f0;");
        grid.add(ico, 0, row); grid.add(lbl, 1, row); grid.add(val, 2, row);
    }

    private Region profileDivider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color: rgba(237,29,36,0.18);");
        VBox.setMargin(r, new Insets(4, 0, 4, 0));
        return r;
    }

    private VBox buildProfileAbilityCard(Ability a) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 6;"
                + " -fx-border-color: rgba(255,255,255,0.07); -fx-border-radius: 6;"
                + " -fx-padding: 8 10 8 10;");
        VBox.setMargin(card, new Insets(0, 14, 6, 14));

        String typeLabel, typeColor, desc;
        if (a instanceof DamagingAbility da) {
            typeLabel = "DMG";  typeColor = "#c0392b";
            desc = "Deals " + da.getDamageAmount() + " damage to target(s)";
        } else if (a instanceof HealingAbility ha) {
            typeLabel = "HEAL"; typeColor = "#1e8449";
            desc = "Restores " + ha.getHealAmount() + " HP to target(s)";
        } else if (a instanceof CrowdControlAbility ca) {
            typeLabel = "CC";   typeColor = "#8e44ad";
            desc = ccEffectDesc(ca.getEffect());
        } else {
            typeLabel = "?";    typeColor = "#555"; desc = "";
        }

        // Row 1: type chip + name
        HBox header = new HBox(7);
        header.setAlignment(Pos.CENTER_LEFT);
        Label chip = new Label(typeLabel);
        chip.setStyle("-fx-background-color: " + typeColor + "; -fx-text-fill: white;"
                + " -fx-font-size: 8px; -fx-font-weight: bold; -fx-padding: 2 5 2 5; -fx-background-radius: 3;");
        Label abilName = new Label(a.getName());
        abilName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #e8e8f8;");
        header.getChildren().addAll(chip, abilName);
        card.getChildren().add(header);

        // Row 2: description
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(200,200,220,0.85); -fx-wrap-text: true;");
        descLabel.setMaxWidth(255);
        card.getChildren().add(descLabel);

        // Row 3: cost / range / cooldown / area
        String aoeStr = switch (a.getCastArea().name()) {
            case "SINGLETARGET" -> "Single";
            case "SURROUND"     -> "Surround";
            case "DIRECTIONAL"  -> "Line";
            case "TEAMTARGET"   -> "Team";
            case "SELFTARGET"   -> "Self";
            default -> a.getCastArea().name();
        };
        Label meta = new Label("\uD83D\uDCA7" + a.getManaCost()
                + "  \u26A1" + a.getRequiredActionPoints()
                + "  \uD83D\uDCCF Range:" + a.getCastRange()
                + "  \u23F3 CD:" + a.getBaseCooldown()
                + "  \u25CE " + aoeStr);
        meta.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(150,160,200,0.65);");
        card.getChildren().add(meta);

        return card;
    }

    private String ccEffectDesc(Effect e) {
        if (e == null) return "Applies a crowd-control effect";
        return switch (e.getClass().getSimpleName()) {
            case "Shield"  -> "Grants Shield — absorbs next hit (" + e.getDuration() + " turns)";
            case "Dodge"   -> "Grants Dodge — evades all attacks (" + e.getDuration() + " turns)";
            case "Embrace" -> "Grants Embrace — reduces incoming damage (" + e.getDuration() + " turns)";
            case "PowerUp" -> "Grants PowerUp — doubles attack damage (" + e.getDuration() + " turns)";
            case "SpeedUp" -> "Grants SpeedUp — +1 action point per turn (" + e.getDuration() + " turns)";
            case "Root"    -> "Roots target — cannot move (" + e.getDuration() + " turns)";
            case "Shock"   -> "Shocks target — halves damage output (" + e.getDuration() + " turns)";
            case "Silence" -> "Silences target — cannot use abilities (" + e.getDuration() + " turns)";
            case "Disarm"  -> "Disarms target — cannot attack (" + e.getDuration() + " turns)";
            case "Stun"    -> "Stuns target — skips their turn (" + e.getDuration() + " turns)";
            default -> "Applies " + e.getClass().getSimpleName() + " (" + e.getDuration() + " turns)";
        };
    }

    private String leaderAbilityDesc(Champion c) {
        if (c instanceof Hero)
            return "Cleanses all negative effects from every ally and grants Embrace — reducing incoming damage for 2 turns.";
        if (c instanceof Villain)
            return "Ruthlessly executes any enemy at 30% HP or below — instantly knocking them out.";
        return "Grants Dodge to the entire team for 3 turns — every ally evades all incoming attacks.";
    }

    private void applyTeams() {
        player1.getTeam().clear();
        player1.getTeam().addAll(p1Selected);
        player1.setLeader(p1Leader != null ? p1Leader : p1Selected.get(0));

        player2.getTeam().clear();
        player2.getTeam().addAll(p2Selected);
        player2.setLeader(p2Leader != null ? p2Leader : p2Selected.get(0));
    }
}
