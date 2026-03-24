package com.marvelwargame.ui.controllers;

import com.marvelwargame.MarvelWarApp;
import com.marvelwargame.ui.util.SoundManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

/**
 * Main menu — three-layer particle field (dust, sparks, scan-line) + sound.
 */
public class MainMenuController {

    @FXML private Pane particleLayer;
    @FXML private StackPane howToPlayOverlay;

    @FXML
    public void initialize() {
        buildParticles();
    }

    @FXML
    private void onNewGame() {
        SoundManager.getInstance().play("confirm");
        try {
            MarvelWarApp.navigateTo("PlayerSetup");
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) cause = cause.getCause();
            System.err.println("[ERROR] navigateTo PlayerSetup failed: " + cause);
        }
    }

    @FXML
    private void onHowToPlay() {
        SoundManager.getInstance().play("click");
        howToPlayOverlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(200), howToPlayOverlay);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    @FXML
    private void onCloseHowToPlay() {
        SoundManager.getInstance().play("back");
        FadeTransition ft = new FadeTransition(Duration.millis(150), howToPlayOverlay);
        ft.setFromValue(1); ft.setToValue(0);
        ft.setOnFinished(e -> howToPlayOverlay.setVisible(false));
        ft.play();
    }

    @FXML
    private void onQuit() {
        SoundManager.getInstance().play("back");
        MarvelWarApp.getStage().close();
    }

    // ── Particle system ───────────────────────────────────────────────────────

    private void buildParticles() {
        Random rng = new Random();

        // Layer 1: 60 small dust particles (red/white/dim)
        for (int i = 0; i < 60; i++) {
            double x = rng.nextDouble() * 1100;
            double y = rng.nextDouble() * 680;
            double r = rng.nextDouble() * 2.2 + 0.4;
            Color col = switch (rng.nextInt(3)) {
                case 0  -> Color.web("#ed1d24", rng.nextDouble() * 0.5 + 0.1);
                case 1  -> Color.web("#f5a623", rng.nextDouble() * 0.3 + 0.05);
                default -> Color.web("#ffffff", rng.nextDouble() * 0.2 + 0.05);
            };
            Circle c = new Circle(x, y, r, col);
            particleLayer.getChildren().add(c);

            TranslateTransition drift = new TranslateTransition(Duration.seconds(rng.nextDouble() * 12 + 6), c);
            drift.setByY(-(rng.nextDouble() * 320 + 80));
            drift.setByX(rng.nextDouble() * 100 - 50);
            drift.setCycleCount(Animation.INDEFINITE);
            drift.setInterpolator(Interpolator.LINEAR);
            drift.setAutoReverse(false);
            drift.setDelay(Duration.seconds(rng.nextDouble() * 10));
            drift.play();

            FadeTransition fade = new FadeTransition(Duration.seconds(rng.nextDouble() * 3.5 + 1.5), c);
            fade.setFromValue(0.05); fade.setToValue(0.65);
            fade.setAutoReverse(true); fade.setCycleCount(Animation.INDEFINITE);
            fade.play();
        }

        // Layer 2: 20 larger bright sparks
        for (int i = 0; i < 20; i++) {
            double x = rng.nextDouble() * 1100;
            double y = rng.nextDouble() * 680;
            double r = rng.nextDouble() * 4.0 + 1.5;
            Circle spark = new Circle(x, y, r, Color.web("#ed1d24", rng.nextDouble() * 0.4 + 0.1));
            particleLayer.getChildren().add(spark);

            ScaleTransition pulse = new ScaleTransition(Duration.seconds(rng.nextDouble() * 2 + 1), spark);
            pulse.setFromX(0.5); pulse.setToX(1.8);
            pulse.setFromY(0.5); pulse.setToY(1.8);
            pulse.setAutoReverse(true); pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setInterpolator(Interpolator.EASE_BOTH);
            pulse.setDelay(Duration.seconds(rng.nextDouble() * 5));
            pulse.play();

            FadeTransition fade2 = new FadeTransition(Duration.seconds(rng.nextDouble() * 2 + 1.5), spark);
            fade2.setFromValue(0.05); fade2.setToValue(0.55);
            fade2.setAutoReverse(true); fade2.setCycleCount(Animation.INDEFINITE);
            fade2.play();
        }

        // Layer 3: 3 subtle horizontal scan-lines
        for (int i = 0; i < 3; i++) {
            double startY = rng.nextDouble() * 680;
            Rectangle scan = new Rectangle(0, startY, 1100, 1.0);
            scan.setFill(Color.web("#ed1d24", 0.04));
            scan.setMouseTransparent(true);
            particleLayer.getChildren().add(scan);

            TranslateTransition scanMove = new TranslateTransition(Duration.seconds(8 + i * 3), scan);
            scanMove.setByY(680);
            scanMove.setAutoReverse(false);
            scanMove.setCycleCount(Animation.INDEFINITE);
            scanMove.setInterpolator(Interpolator.LINEAR);
            scanMove.setDelay(Duration.seconds(i * 2.5));
            scanMove.play();
        }
    }
}
