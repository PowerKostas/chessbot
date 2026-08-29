package com.chessbot.ui.components;

import com.chessbot.engine.core.Piece;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.function.Consumer;

public final class PromotionDialog {
    private PromotionDialog() {}


    public static void display(VisualBoard visualBoard, int color, boolean reversed, Consumer<Integer> callback) {
        // Clears the legal hints before showing the dialog
        visualBoard.clearLegalHints();

        // Creates an overlay that spans across the whole board to capture outside clicks. It's the deepest layer, if the user
        // clicks it, not the top-level dialog container, close the promotion dialog
        StackPane overlay = new StackPane();
        overlay.setOnMousePressed(e -> {
            if (e.getTarget() == overlay) {
                visualBoard.getChildren().remove(overlay);
                callback.accept(-1);
                e.consume();
            }
        });

        // Builds the dialog's container
        HBox layout = new HBox();
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #ebecd0; -fx-border-color: #739552; -fx-border-width: 5; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-insets: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 5);");
        layout.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        overlay.getChildren().add(layout);

        // Adds the 4 piece choices to the HBox, in the order of chess.com
        int[] pieceTypes = {Piece.QUEEN, Piece.KNIGHT, Piece.ROOK, Piece.BISHOP};
        for (int type : pieceTypes) {
            VisualPiece piece = new VisualPiece(color, type, reversed);
            piece.setScaleX(0.9);
            piece.setScaleY(0.9);

            StackPane pieceWrapper = new StackPane(piece);
            pieceWrapper.setPadding(new Insets(8));
            HBox.setHgrow(pieceWrapper, Priority.ALWAYS);
            pieceWrapper.setMaxWidth(Double.MAX_VALUE);
            pieceWrapper.setCursor(Cursor.HAND);

            // Adds hover effect
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), piece);
            scaleUp.setToX(1);
            scaleUp.setToY(1);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), piece);
            scaleDown.setToX(0.9);
            scaleDown.setToY(0.9);

            pieceWrapper.setOnMouseEntered(e -> scaleUp.playFromStart());
            pieceWrapper.setOnMouseExited(e -> scaleDown.playFromStart());

            // Closes the popup on piece choice click
            pieceWrapper.setOnMouseClicked(e -> {
                visualBoard.getChildren().remove(overlay);
                callback.accept(type);
                e.consume();
            });

            layout.getChildren().add(pieceWrapper);
        }

        // Adds the close button
        Button cancelButton = new Button("✕");
        cancelButton.setPadding(new Insets(8));
        HBox.setHgrow(cancelButton, Priority.ALWAYS);
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxHeight(Double.MAX_VALUE);

        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #739552; -fx-font-weight: bold; -fx-font-size: 32px; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 32px; -fx-cursor: hand;";
        cancelButton.setStyle(defaultStyle);
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(hoverStyle));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(defaultStyle));

        cancelButton.setOnAction(e -> {
            visualBoard.getChildren().remove(overlay);
            callback.accept(-1);
            e.consume();
        });

        layout.getChildren().add(cancelButton);

        // Adds the promotion dialog to the visual board
        visualBoard.getChildren().add(overlay);
    }
}
