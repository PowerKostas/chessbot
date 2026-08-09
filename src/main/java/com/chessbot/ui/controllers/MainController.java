package com.chessbot.ui.controllers;

import com.chessbot.ui.components.Square;
import com.chessbot.ui.components.VisualBoard;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

// This class runs after view.fxml is loaded from ChessApplication
public class MainController {
    @FXML
    private HBox mainContainer;

    private VisualBoard boardOne;
    private VisualBoard boardTwo;

    // Global reference
    public static MainController instance;


    public void initialize() {
        instance = this;

        boardTwo = new VisualBoard(0, "4k/8/8/8/8/8/8/4K"); // Kings are needed for the visualization board to not crash
        boardOne = new VisualBoard(0, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

        mainContainer.getChildren().addAll(boardOne, boardTwo);
    }


    public void bitboardVisualization(long bitboard) {
        bitboard = Long.reverseBytes(bitboard);

        if (boardTwo == null) {
            return;
        }

        for (int i = 0; i < 64; i += 1) {
            Square square = (Square) boardTwo.getChildren().get(i);

            long mask = 1L << i;
            if ((bitboard & mask) != 0) {
                square.setStyle("-fx-background-color: red");
            }

            else {
                if ((square.getRow() + square.getCol()) % 2 == 0) {
                    square.setStyle("-fx-background-color: #ebecd0");
                }

                else {
                    square.setStyle("-fx-background-color: #739552");
                }
            }
        }
    }
}
