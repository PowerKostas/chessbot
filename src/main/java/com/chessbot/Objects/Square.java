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
    private boolean isSelected = false;
    private boolean isRightClicked = false;


    public Square(int row, int col, Board board) {
        this.row = row;
        this.col = col;

        // Adds square colour
        this.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");

        // If the player is white, put the letters at the left column, if the player is black (board will be reversed)
        // put the letters in the right column
        if ((board.getPlayerColour() == 0 && col == 0) || (board.getPlayerColour() == 1 && col == 7)) {
            Label number = new Label();

            if ((row + col) % 2 == 0) {
                number.setTextFill(Color.web("#739552"));
            }

            else {
                number.setTextFill(Color.web("#ebecd0"));
            }

            number.setText(Integer.toString(8 - row));
            number.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
            StackPane.setAlignment(number, Pos.TOP_LEFT);
            StackPane.setMargin(number, new Insets(0, 0, 0, 4));

            // If the player is black, reverse the numbers
            if (board.getPlayerColour() == 1) {
                number.setRotate(180);
                StackPane.setAlignment(number, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(number, new Insets(0, 4, 0, 0));
            }

            else {
                StackPane.setAlignment(number, Pos.TOP_LEFT);
                StackPane.setMargin(number, new Insets(0, 0, 0, 4));
            }

            this.getChildren().add(number);
        }

        // If the player is white, put the letters at the bottom row, if the player is black (board will be reversed)
        // put the letters in the top row
        if ((board.getPlayerColour() == 0 && row == 7) || (board.getPlayerColour() == 1 && row == 0)) {
            Label letter = new Label();

            if ((row + col) % 2 == 0) {
                letter.setTextFill(Color.web("#739552"));
            }

            else {
                letter.setTextFill(Color.web("#ebecd0"));
            }


            letter.setText(String.valueOf((char) ('a' + col)));
            letter.setStyle("-fx-font-size: 16; -fx-font-weight: bold");

            // If the player is black, reverse the letters
            if (board.getPlayerColour() == 1) {
                letter.setRotate(180);
                StackPane.setAlignment(letter, Pos.TOP_LEFT);
                StackPane.setMargin(letter, new Insets(0, 0, 0, 4));
            }

            else {
                StackPane.setAlignment(letter, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(letter, new Insets(0, 4, 0, 0));
            }

            this.getChildren().add(letter);
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

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Boolean getIsRightClicked() {
        return isRightClicked;
    }

    public void setIsRightClicked(boolean isRightClicked) {
        this.isRightClicked = isRightClicked;
    }


    public void setStyle(String lightSquareStyle, String darkSquareStyle) {
        if ((this.row + this.col) % 2 == 0) { // if light square
            this.setStyle(lightSquareStyle);
        }

        else { // if dark square
            this.setStyle(darkSquareStyle);
        }
    }
}
