package com.chessbot;

import com.chessbot.Objects.Board;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class ViewManager {
    @FXML
    private HBox mainContainer;

    private Board boardOne;
    private Board boardTwo;


    public void initialize() {
        boardOne = new Board();
        boardTwo = new Board();
        mainContainer.getChildren().addAll(boardOne, boardTwo);

        boardOne.setBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        boardTwo.setBoard("8/8/8/8/8/8/8/8");
        boardTwo.bitboardVisualization(boardOne.getAllPiecesBitboard());
    }
}