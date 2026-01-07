package com.chessbot;

import com.chessbot.Objects.Board;
import javafx.fxml.FXML;

public class ViewManager {
    @FXML
    private Board boardOneController;

    @FXML
    private Board boardTwoController;


    public void initialize() {
        boardOneController.setBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        boardTwoController.setBoard("8/8/8/8/8/8/8/8");
        boardTwoController.bitboardVisualization(boardOneController.getAllPiecesBitboard());
    }
}
