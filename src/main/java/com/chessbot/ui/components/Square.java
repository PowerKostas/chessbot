package com.chessbot.ui.components;

import com.chessbot.engine.core.Piece;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

// The children of this class are number coordinates, letter coordinates and a VisualPiece
public class Square extends StackPane {
    private final int row;
    private final int col;
    private final Circle legalMoveHint;
    private final Region legalCaptureHint;
    private VisualPiece currentPiece;
    private boolean isSelected = false;
    private boolean isPreviousMove = false;
    private boolean isRightClicked = false;


    public Square(int row, int col, VisualBoard board) {
        this.row = row;
        this.col = col;

        // Adds a circle that indicates a legal move and makes it invisible
        legalMoveHint = new Circle(14, Color.web("#000000", 0.2));
        legalMoveHint.setMouseTransparent(true);
        legalMoveHint.setVisible(false);
        this.getChildren().add(legalMoveHint);

        // Creates a transparent circle and because the circle's corners and edges are further away than the circle's radius, it
        // fills them with the appropriate color. All this indicates a legal capture, it's invisible to start with. center
        // 50% 50% = Puts the circle in the middle of the square, radius 45% = The radius of the circle, transparent 98% = Makes
        // the gradient transparent until 98% of the gradient's radius, #00000033 100% = Makes the outer edger of the gradient's
        // radius #000000 with 0.2 transparency, just like legalMoveHint
        legalCaptureHint = new Region();
        legalCaptureHint.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 45%, transparent 98%, #00000033 100%);");
        legalCaptureHint.setMouseTransparent(true);
        legalCaptureHint.setVisible(false);
        this.getChildren().add(legalCaptureHint);

        // Adds square color
        this.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");

        // If the player is white, put the numbers at the left column, if the player is black (board will be reversed)
        // put the numbers in the right column
        if ((board.getPlayerColor() == Piece.WHITE && col == 0) || (board.getPlayerColor() == Piece.BLACK && col == 7)) {
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
            if (board.getPlayerColor() == Piece.BLACK) {
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
        if ((board.getPlayerColor() == Piece.WHITE && row == 7) || (board.getPlayerColor() == Piece.BLACK && row == 0)) {
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
            if (board.getPlayerColor() == Piece.BLACK) {
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

    public VisualPiece getCurrentPiece() {
        return currentPiece;
    }

    public void setCurrentPiece(VisualPiece currentPiece) {
        this.currentPiece = currentPiece;

        if (currentPiece != null) { // When a piece gets removed from a square, VisualBoard.sync returns null, so a check is needed
            this.getChildren().add(currentPiece);
            this.setCursor(Cursor.HAND);
        }

        else {
            this.setCursor(Cursor.DEFAULT);
        }
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
        this.updateColor();
    }

    public Boolean getIsPreviousMove() { return isPreviousMove; }

    public void setIsPreviousMove(boolean isPreviousMove) {
        this.isPreviousMove = isPreviousMove;
        this.updateColor();
    }

    public Boolean getIsRightClicked() {
        return isRightClicked;
    }

    public void setIsRightClicked(boolean isRightClicked) {
        this.isRightClicked = isRightClicked;
        this.updateColor();
    }


    // Sets the style of the square
    public void setStyle(String lightSquareStyle, String darkSquareStyle) {
        if ((this.row + this.col) % 2 == 0) { // If light square
            this.setStyle(lightSquareStyle);
        }

        else { // If dark square
            this.setStyle(darkSquareStyle);
        }
    }


    // Updates the color of the square
    public void updateColor() {
        // If the square is right-clicked
        if (isRightClicked) {
            this.setStyle("-fx-background-color: #eb7d6a", "-fx-background-color: #d36c50");
        }

        // If the square is selected or if a move affected this square
        else if (isSelected || isPreviousMove) {
            this.setStyle("-fx-background-color: #f5f682", "-fx-background-color: #b9ca43");
        }

        // If a move was made, and it doesn't affect this square (used to reset the color of a hovered square or old previous
        // move squares). Or if a left/right click happened on the board (a left click resets the right-clicked and selected
        // squares colors and a right click resets the selected square color)
        else {
            this.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
        }
    }


    // Updates the legal move/capture hint visibility in the square
    public void updateLegalHint(boolean isLegalMove, boolean isLegalCapture) {
        this.legalMoveHint.setVisible(isLegalMove);
        this.legalCaptureHint.setVisible(isLegalCapture);
    }
}
