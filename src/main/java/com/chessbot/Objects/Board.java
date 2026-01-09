package com.chessbot.Objects;

import com.chessbot.BoardUtils.RightClick;
import com.chessbot.BoardUtils.DragMove;
import com.chessbot.BoardUtils.FenReader;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

// Board class that is used to represent the board in JavaFX and the board state
public class Board extends GridPane {
    // 12 64 bit variables, one for each piece colour and piece type, indexing: Piece colour * 6 + Piece pieceType, that
    // each bit represents a piece on the board, used for board representation
    private final long[] bitboards = new long[12];


    public Board() {
        // Sets up FXML
        this.setPrefSize(700, 700);

        for (int i = 0; i < 8; i++) {
            ColumnConstraints colConstraint = new ColumnConstraints();
            colConstraint.setPercentWidth(12.5);
            this.getColumnConstraints().add(colConstraint);
        }

        for (int i = 0; i < 8; i++) {
            RowConstraints rowConstraint = new RowConstraints();
            rowConstraint.setPercentHeight(12.5);
            this.getRowConstraints().add(rowConstraint);
        }

        // One instance of the classes for every board
        DragMove dragMove = new DragMove(this);
        RightClick rightClick = new RightClick();

        // Prepares every custom square class for the JavaFX board
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                Square square = new Square(row, col);

                // Makes every square draggable/clickable, more info on the specific classes
                square.setOnDragDetected(dragMove::dragDetected);
                square.setOnDragOver(dragMove::drag);
                square.setOnDragEntered(dragMove::dragEnter);
                square.setOnDragExited(dragMove::dragExit);
                square.setOnDragDropped(dragMove::dragDropped);
                square.setOnDragDone(dragMove::dragDone);

                square.setOnMouseClicked(rightClick::click);

                this.add(square, col, row);
            }
        }
    }


    public long getBitboard(int index) {
        return bitboards[index];
    }

    public void setBitboard(int index, long bitboard) {
        this.bitboards[index] = bitboard;
    }


    public void setBoard(String fenSequence) {
        FenReader.build(fenSequence, this);
    }


    public void bitboardVisualization(long bitboard) {
        for (int i = 0; i < 64; i += 1) {
            Square square = (Square) this.getChildren().get(i);

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
