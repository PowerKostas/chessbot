package com.chessbot.BoardUtils;

import com.chessbot.ChessApplication;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.media.AudioClip;

public class DragMove {
    private ImageView draggedPiece;
    private StackPane startingSquare;
    private StackPane hoveredSquare;
    private StackPane endingSquare;
    private String previousHoveredSquareStyle;
    private String tempStartingSquareStyle;
    private String tempEndingSquareStyle;
    private StackPane previousStartingSquare;
    private String previousStartingSquareStyle;
    private StackPane previousEndingSquare;
    private String previousEndingSquareStyle;
    private StackPane failedStartingSquare;
    private String failedStartingSquareStyle;


    public void dragDetected(MouseEvent event) {
        if (failedStartingSquare != null) {
            failedStartingSquare.setStyle(failedStartingSquareStyle);
        }

        startingSquare = (StackPane) event.getSource();

        if (startingSquare.getChildren().isEmpty()) {
            event.consume();
            return;
        }

        draggedPiece = (ImageView) startingSquare.getChildren().getLast();

        Dragboard db = startingSquare.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString("piece_move");
        db.setContent(content);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image pieceImage = draggedPiece.snapshot(params, null);
        db.setDragView(pieceImage);
        db.setDragViewOffsetX(pieceImage.getWidth() / 2);
        db.setDragViewOffsetY(pieceImage.getHeight() / 2);
        startingSquare.setViewOrder(-1);

        draggedPiece.setVisible(false);

        tempStartingSquareStyle = startingSquare.getStyle();

        if (tempStartingSquareStyle.equals("-fx-background-color: #ebecd0") || tempStartingSquareStyle.equals("-fx-background-color: #f5f682")) {
            startingSquare.setStyle("-fx-background-color: #f5f682");
        }

        else {
            startingSquare.setStyle("-fx-background-color: #b9ca43");
        }

        event.consume();
    }


    public void drag(DragEvent event) {
        if (event.getGestureSource() != event.getSource() && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }

        event.consume();
    }


    public void dragEnter(DragEvent event) {
        hoveredSquare = (StackPane) event.getSource();
        previousHoveredSquareStyle = hoveredSquare.getStyle();

        if (previousHoveredSquareStyle.equals("-fx-background-color: #ebecd0")) {
            hoveredSquare.setStyle(previousHoveredSquareStyle + "; -fx-border-color: #f8f8ef; -fx-border-width: 4; -fx-padding: -4;");
        }

        else {
            hoveredSquare.setStyle(previousHoveredSquareStyle + "; -fx-border-color: #cedac3; -fx-border-width: 4; -fx-padding: -4;");
        }

        event.consume();
    }


    public void dragDropped(DragEvent event) {
        endingSquare = (StackPane) event.getSource();
        endingSquare.getChildren().add(draggedPiece);
        endingSquare.setCursor(Cursor.HAND);
        tempEndingSquareStyle = endingSquare.getStyle().split(";")[0];

        //BoardController.all_pieces_bitboard += 1L << (GridPane.getRowIndex(endingSquare) * 8L + GridPane.getColumnIndex(endingSquare));
        //BoardController.all_pieces_bitboard -= 1L << (GridPane.getRowIndex(startingSquare) * 8L + GridPane.getColumnIndex(startingSquare));

        //TwoBoardsController.instance.test();

        draggedPiece.setVisible(true);

        if (previousStartingSquare != null && previousEndingSquare != null) {
            previousStartingSquare.setStyle(previousStartingSquareStyle);
            previousEndingSquare.setStyle(previousEndingSquareStyle);
        }

        AudioClip clickSound = new AudioClip(ChessApplication.class.getResource("Sounds/move-self.mp3").toString());
        clickSound.play();

        event.setDropCompleted(true);
        event.consume();
    }


    public void dragExit(DragEvent event) {
        if (hoveredSquare != endingSquare) {
            hoveredSquare.setStyle(previousHoveredSquareStyle);
        }

        else {
            if (previousHoveredSquareStyle.equals("-fx-background-color: #ebecd0")) {
                hoveredSquare.setStyle("-fx-background-color: #f5f682");
            }

            else {
                hoveredSquare.setStyle("-fx-background-color: #b9ca43");
            }
        }

        event.consume();
    }


    public void dragDone(DragEvent event) {
        if (event.getTransferMode() == TransferMode.MOVE) {
            startingSquare.setCursor(Cursor.DEFAULT);

            previousStartingSquare = startingSquare;
            previousStartingSquareStyle = tempStartingSquareStyle;
            previousEndingSquare = endingSquare;
            previousEndingSquareStyle = tempEndingSquareStyle;

            failedStartingSquare = null;
            failedStartingSquareStyle = null;
            endingSquare = null;
        }

        else {
            draggedPiece.setVisible(true);

            failedStartingSquare = startingSquare;
            failedStartingSquareStyle = tempStartingSquareStyle;
        }

        event.consume();
    }
}
