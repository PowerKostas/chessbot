package com.chessbot;

import com.chessbot.Objects.Board;
import com.chessbot.Objects.Square;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

// This class runs after the view.fxml is loaded from ChessApplication
public class ViewManager {
    @FXML
    private HBox mainContainer;

    private Board boardOne;
    private Board boardTwo;

    // Global reference
    public static ViewManager instance;


    public void initialize() {
        instance = this;

        boardTwo = new Board("8/8/8/8/8/8/8/8");
        boardOne = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        mainContainer.getChildren().addAll(boardOne, boardTwo);
    }


    public void bitboardVisualization(long bitboard) {
        if (boardTwo == null) {
            return;
        }

        for (int i = 0; i < 64; i += 1) {
            Square square = (Square) boardTwo.getChildren().get(i);

            long mask = 1L << i;
            if ((bitboard & mask) != 0) {
                square.setStyle("-fx-background-color: red");
            } else {
                if ((square.getRow() + square.getCol()) % 2 == 0) {
                    square.setStyle("-fx-background-color: #ebecd0");
                } else {
                    square.setStyle("-fx-background-color: #739552");
                }
            }
        }
    }
}
