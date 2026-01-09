package com.chessbot;

import com.chessbot.Objects.Board;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class ViewManager {
    @FXML
    private HBox mainContainer;

    private Board boardOne;
    private Board boardTwo;

    // Global reference
    public static ViewManager instance;


    public void initialize() {
        instance = this;

        boardOne = new Board();
        boardTwo = new Board();
        mainContainer.getChildren().addAll(boardOne, boardTwo);

        boardOne.setBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        boardTwo.setBoard("8/8/8/8/8/8/8/8");
        callBitboardVisualization();
    }


    public void callBitboardVisualization() {
        boardTwo.bitboardVisualization(boardOne.getAllPiecesBitboard());
    }
}