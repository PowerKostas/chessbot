module com.chessbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.chessbot.Controllers to javafx.fxml;
    opens com.chessbot to javafx.fxml;
    exports com.chessbot;
}