package com.marvelwargame.ui.controllers;

import com.marvelwargame.MarvelWarApp;
import com.marvelwargame.engine.Player;
import com.marvelwargame.ui.util.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Collects player names and moves to champion selection.
 */
public class PlayerSetupController {

    @FXML private TextField tfPlayer1;
    @FXML private TextField tfPlayer2;

    @FXML
    private void onBack() {
        SoundManager.getInstance().play("back");
        try { MarvelWarApp.navigateTo("MainMenu"); }
        catch (Exception e) { printError(e); }
    }

    @FXML
    private void onNext() {
        SoundManager.getInstance().play("confirm");
        String name1 = tfPlayer1.getText().trim();
        String name2 = tfPlayer2.getText().trim();
        if (name1.isEmpty()) name1 = "Player 1";
        if (name2.isEmpty()) name2 = "Player 2";

        Player p1 = new Player(name1);
        Player p2 = new Player(name2);
        try {
            MarvelWarApp.navigateWithController("ChampionSelect", ctrl ->
                    ((ChampionSelectController) ctrl).initForPlayer(p1, p2));
        } catch (Exception e) { printError(e); }
    }

    private static void printError(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) cause = cause.getCause();
        System.err.println("[ERROR] Navigation failed: " + cause);
        cause.printStackTrace(System.err);
    }
}
