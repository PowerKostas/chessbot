module com.chessbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;

    opens com.chessbot.Controllers to javafx.fxml;
    opens com.chessbot to javafx.fxml;
    exports com.chessbot;
}