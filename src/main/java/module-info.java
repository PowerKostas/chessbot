module com.chessbot {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.chessbot to javafx.fxml;
    exports com.chessbot;
}