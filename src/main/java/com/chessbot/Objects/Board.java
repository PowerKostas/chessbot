package com.chessbot.Objects;

import com.chessbot.BoardUtils.DragMove;
import com.chessbot.BoardUtils.FenReader;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

// Board class that is used to represent the board in JavaFX and the board state
public class Board extends GridPane {
    // 64 bit variable that each bit represent a piece on the board, used for board representation
    private long allPiecesBitboard;


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

        // One instance of the class for every board
        DragMove dragMove = new DragMove(this);

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

                this.add(square, col, row);
            }
        }
    }


    public long getAllPiecesBitboard() {
        return allPiecesBitboard;
    }

    public void setAllPiecesBitboard(long allPiecesBitboard) {
        this.allPiecesBitboard = allPiecesBitboard;
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