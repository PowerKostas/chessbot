package com.chessbot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ChessApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChessApplication.class.getResource("board.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("chessbot");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
