package com.chessbot.Objects;

import com.chessbot.BoardUtils.FenReader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class BoardPieces {
    private final FenReader fenReader = new FenReader();

    public long allPiecesBitboard;


    public void setBoard(String fenSequence, GridPane board) {
        allPiecesBitboard = fenReader.build(fenSequence, board);
    }


    public void bitboardVisualization(GridPane board) {
        for (int i = 0; i < 64; i += 1) {
            StackPane square = (StackPane) board.getChildren().get(i);

            long mask = 1L << i;
            if ((allPiecesBitboard & mask) != 0) {
                square.setStyle("-fx-background-color: red");
            }

            else {
                if ((GridPane.getRowIndex(square) + GridPane.getColumnIndex(square)) % 2 == 0) {
                    square.setStyle("-fx-background-color: #ebecd0");
                }

                else {
                    square.setStyle("-fx-background-color: #739552");
                }
            }
        }
    }
}
