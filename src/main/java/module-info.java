module com.chessbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;

    exports com.chessbot;
    opens com.chessbot to javafx.fxml;
    opens com.chessbot.ui.components to javafx.fxml;
    opens com.chessbot.ui.input to javafx.fxml;
    opens com.chessbot.engine.core to javafx.fxml;
    opens com.chessbot.engine.core.Pieces to javafx.fxml;
    opens com.chessbot.engine.utils to javafx.fxml;
    exports com.chessbot.ui.controllers;
    opens com.chessbot.ui.controllers to javafx.fxml;
}
