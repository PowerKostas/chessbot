package com.chessbot.Objects;

import com.chessbot.BoardUtils.DragMove;
import com.chessbot.BoardUtils.FenReader;
import javafx.scene.layout.GridPane;

public class Board extends GridPane {
    // 64 bit variable that each bit represent a piece on the board, used for board representation
    private long allPiecesBitboard;

    private final DragMove dragMove = new DragMove();


    // Prepares every custom square class for the JavaFX board
    public Board() {
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                Square square = new Square(row, col, dragMove);
                this.add(square, col, row);
            }
        }
    }


    public long getAllPiecesBitboard() {
        return allPiecesBitboard;
    }

    public void setBoard(String fenSequence) {
        allPiecesBitboard = FenReader.build(fenSequence, this);
    }


    public void bitboardVisualization(long allPiecesBitboard) {
        for (int i = 0; i < 64; i += 1) {
            Square square = (Square) this.getChildren().get(i);

            long mask = 1L << i;
            if ((allPiecesBitboard & mask) != 0) {
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
