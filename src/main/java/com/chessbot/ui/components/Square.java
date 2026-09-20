package com.chessbot.ui.components;

import com.chessbot.engine.core.Piece;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Square extends StackPane {
    private final int row;
    private final int col;
    private Label numberCoordinates;
    private Label letterCoordinates;
    private final Circle legalMoveHint;
    private final Region legalCaptureHint;
    private VisualPiece currentPiece;
    private boolean isSelected = false;
    private boolean isPreviousMove = false;
    private boolean isRightClicked = false;


    public Square(int row, int col, VisualBoard visualBoard) {
        this.row = row;
        this.col = col;

        applySquareStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
        updateCoordinateLabels(visualBoard.getBoardPerspective());

        // Adds a circle that indicates a legal move and makes it invisible
        legalMoveHint = new Circle(14, Color.web("#000000", 0.2));
        legalMoveHint.setMouseTransparent(true);
        legalMoveHint.setVisible(false);
        this.getChildren().add(legalMoveHint);

        // Creates a legal capture indication. Start with a transparent circle, and because the circle's corners and edges
        // are further away than the circle's radius, it fills them with the appropriate color. center 50% 50% = Puts the
        // circle in the middle of the square, radius 45% = The radius of the circle, transparent 98% = Makes the gradient
        // transparent until 98% of the gradient's radius, #00000033 100% = Makes the outer edger of the gradient's radius
        // #000000 with 0.2 transparency, just like legalMoveHint. It's invisible to start with
        legalCaptureHint = new Region();
        legalCaptureHint.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 45%, transparent 98%, #00000033 100%);");
        legalCaptureHint.setMouseTransparent(true);
        legalCaptureHint.setVisible(false);
        this.getChildren().add(legalCaptureHint);

        // If a right click happens, toggle the square's right-clicked status
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                this.setIsRightClicked(!this.getIsRightClicked());
            }
        });
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

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
        updateColor();
    }

    public void setIsPreviousMove(boolean isPreviousMove) {
        this.isPreviousMove = isPreviousMove;
        updateColor();
    }

    public Boolean getIsRightClicked() {
        return isRightClicked;
    }

    public void setIsRightClicked(boolean isRightClicked) {
        this.isRightClicked = isRightClicked;
        updateColor();
    }


    public void applySquareStyle(String lightSquareStyle, String darkSquareStyle) {
        if ((row + col) % 2 == 0) { // If light square
            this.setStyle(lightSquareStyle);
        }

        else { // If dark square
            this.setStyle(darkSquareStyle);
        }
    }


    public void updateColor() {
        // If the square is right-clicked
        if (isRightClicked) {
            applySquareStyle("-fx-background-color: #eb7d6a", "-fx-background-color: #d36c50");
        }

        // If the square is selected or if a move affected this square
        else if (isSelected || isPreviousMove) {
            applySquareStyle("-fx-background-color: #f5f682", "-fx-background-color: #b9ca43");
        }

        // If a move was made, and it doesn't affect this square (used to reset the color of a hovered square or old previous
        // move squares). Or if a left/right click happened on the board (a left click resets the right-clicked and selected
        // squares colors and a right click resets the selected square color)
        else {
            applySquareStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
        }
    }


    // The coordinate labels need to be updated every time the board is flipped
    public void updateCoordinateLabels(int boardPerspective) {
        // Clears the old coordinates if needed
        if (numberCoordinates != null) {
            this.getChildren().remove(numberCoordinates);
            numberCoordinates = null;
        }

        if (letterCoordinates != null) {
            this.getChildren().remove(letterCoordinates);
            letterCoordinates = null;
        }

        // If the player is white, put the numbers at the left column, if the player is black (board will be reversed) put
        // the numbers in the right column
        if ((boardPerspective == Piece.WHITE && col == 0) || (boardPerspective == Piece.BLACK && col == 7)) {
            numberCoordinates = new Label();
            numberCoordinates.setTextFill((row + col) % 2 == 0 ? Color.web("#739552") : Color.web("#ebecd0"));
            numberCoordinates.setText(Integer.toString(8 - row));
            numberCoordinates.setStyle("-fx-font-size: 16; -fx-font-weight: bold");

            // If the player is black also reverse the numbers
            if (boardPerspective == Piece.BLACK) {
                numberCoordinates.setRotate(180);
                StackPane.setAlignment(numberCoordinates, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(numberCoordinates, new Insets(0, 4, 0, 0));
            }

            else {
                numberCoordinates.setRotate(0);
                StackPane.setAlignment(numberCoordinates, Pos.TOP_LEFT);
                StackPane.setMargin(numberCoordinates, new Insets(0, 0, 0, 4));
            }

            this.getChildren().add(numberCoordinates);
        }

        // If the player is white, put the letters at the bottom row, if the player is black (board will be reversed) put
        // the letters in the top row
        if ((boardPerspective == Piece.WHITE && row == 7) || (boardPerspective == Piece.BLACK && row == 0)) {
            letterCoordinates = new Label();
            letterCoordinates.setTextFill((row + col) % 2 == 0 ? Color.web("#739552") : Color.web("#ebecd0"));
            letterCoordinates.setText(String.valueOf((char) ('a' + col)));
            letterCoordinates.setStyle("-fx-font-size: 16; -fx-font-weight: bold");

            // If the player is black also reverse the letters
            if (boardPerspective == Piece.BLACK) {
                letterCoordinates.setRotate(180);
                StackPane.setAlignment(letterCoordinates, Pos.TOP_LEFT);
                StackPane.setMargin(letterCoordinates, new Insets(0, 0, 0, 4));
            }

            else {
                letterCoordinates.setRotate(0);
                StackPane.setAlignment(letterCoordinates, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(letterCoordinates, new Insets(0, 4, 0, 0));
            }

            this.getChildren().add(letterCoordinates);
        }
    }


    public void toggleLegalHint(boolean isLegalMove, boolean isLegalCapture) {
        legalMoveHint.setVisible(isLegalMove);
        legalCaptureHint.setVisible(isLegalCapture);
    }
}
