module com.marvelwargame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires java.logging;            // for java.awt.Point
    opens com.marvelwargame to javafx.fxml;
    opens com.marvelwargame.ui.controllers to javafx.fxml;
    opens com.marvelwargame.ui.components  to javafx.fxml;
    opens com.marvelwargame.engine         to javafx.fxml;
    opens com.marvelwargame.model.world    to javafx.fxml;
    opens com.marvelwargame.model.abilities to javafx.fxml;
    opens com.marvelwargame.model.effects  to javafx.fxml;

    exports com.marvelwargame;
    exports com.marvelwargame.engine;
    exports com.marvelwargame.engine.events;
    exports com.marvelwargame.engine.factory;
    exports com.marvelwargame.model.world;
    exports com.marvelwargame.model.abilities;
    exports com.marvelwargame.model.effects;
    exports com.marvelwargame.exceptions;
    exports com.marvelwargame.ui.controllers;
    exports com.marvelwargame.ui.components;
    exports com.marvelwargame.ui.util;
}
