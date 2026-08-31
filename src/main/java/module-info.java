module com.chessbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;

    exports com.chessbot.application;
    exports com.chessbot.ui.components;
    exports com.chessbot.ui.controllers;
    exports com.chessbot.ui.input;
    exports com.chessbot.engine.core;
    exports com.chessbot.engine.movegen;

    opens com.chessbot.application to javafx.fxml;
    opens com.chessbot.ui.components to javafx.fxml;
    opens com.chessbot.ui.controllers to javafx.fxml;
    opens com.chessbot.ui.input to javafx.fxml;
    opens com.chessbot.ui.utils to javafx.fxml;
    opens com.chessbot.Views to javafx.fxml;
}
