package com.chessbot.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

// This class extends the JavaFX StackPane class, it has 3 possible children, number coord, letter coord and
// Piece piece, used for the JavaFX board. I added some attributes for easier calculations.
public class Square extends StackPane {
    private final int row;
    private final int col;
    private Piece currentPiece;


    public Square(int row, int col) {
        this.row = row;
        this.col = col;

        // Adds square colour
        if ((row + col) % 2 == 0) {
            this.setStyle("-fx-background-color: #ebecd0");
        }

        else {
            this.setStyle("-fx-background-color: #739552");
        }

        // Adds number coordinates
        if (col == 0) {
            Label number = new Label();

            if (row % 2 == 0) {
                number.setTextFill(Color.web("#739552"));
            }

            else {
                number.setTextFill(Color.web("#ebecd0"));
            }

            number.setText(Integer.toString(8 - row));
            number.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
            StackPane.setAlignment(number, Pos.TOP_LEFT);
            StackPane.setMargin(number, new Insets(0, 0, 0, 4));

            this.getChildren().add(number);
        }

        // Adds letter coordinates
        if (row == 7) {
            Label number = new Label();

            if (col % 2 == 0) {
                number.setTextFill(Color.web("#ebecd0"));
            } else {
                number.setTextFill(Color.web("#739552"));
            }

            number.setText(String.valueOf((char) ('a' + col)));
            number.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
            StackPane.setAlignment(number, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(number, new Insets(0, 4, 0, 0));

            this.getChildren().add(number);
        }
    }


    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Piece getCurrentPiece() {
        return currentPiece;
    }

    public void setCurrentPiece(Piece currentPiece) {
        this.currentPiece = currentPiece;
        this.getChildren().add(currentPiece);
    }


    public void colour(String lightSquareStyle, String darkSquareStyle) {
        if ((this.row + this.col) % 2 == 0) { // if light square
            this.setStyle(lightSquareStyle);
        }

        else { // if dark square
            this.setStyle(darkSquareStyle);
        }
    }
}
