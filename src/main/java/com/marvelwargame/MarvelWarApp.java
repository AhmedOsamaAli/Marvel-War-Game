package com.marvelwargame;

import com.marvelwargame.engine.factory.AbilityFactory;
import com.marvelwargame.engine.factory.ChampionFactory;
import com.marvelwargame.model.abilities.Ability;
import com.marvelwargame.model.world.Champion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * JavaFX entry point. Manages scene transitions and shared application data.
 */
public class MarvelWarApp extends Application {

    private static Stage primaryStage;
    private static List<Champion> availableChampions;
    private static List<Ability> availableAbilities;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        loadGameData();

        stage.setTitle("Marvel War Game");
        stage.setResizable(false);

        // Optional window icon
        try (InputStream icon = getClass().getResourceAsStream("/images/app_icon.png")) {
            if (icon != null) stage.getIcons().add(new Image(icon));
        } catch (Exception ignored) {}

        navigateTo("MainMenu");
        stage.show();
    }

    /** Parse the CSV data files bundled in resources. */
    private void loadGameData() throws Exception {
        try (InputStream abStream = getClass().getResourceAsStream("/data/Abilities.csv")) {
            Objects.requireNonNull(abStream, "Abilities.csv not found in resources");
            availableAbilities = AbilityFactory.loadFromCsv(abStream);
        }
        try (InputStream chStream = getClass().getResourceAsStream("/data/Champions.csv")) {
            Objects.requireNonNull(chStream, "Champions.csv not found in resources");
            availableChampions = ChampionFactory.loadFromCsv(chStream, availableAbilities);
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public static void navigateTo(String fxmlName) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MarvelWarApp.class.getResource("/fxml/" + fxmlName + ".fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void navigateWithController(String fxmlName,
            java.util.function.Consumer<Object> configureController) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MarvelWarApp.class.getResource("/fxml/" + fxmlName + ".fxml"));
        Scene scene = new Scene(loader.load());
        configureController.accept(loader.getController());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    // ── Global State ──────────────────────────────────────────────────────────

    public static List<Champion> getAvailableChampions() { return availableChampions; }
    public static Stage getStage()                       { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
