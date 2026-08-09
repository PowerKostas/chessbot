package com.chessbot.ui.input;

import com.chessbot.ChessApplication;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.ui.components.Square;
import com.chessbot.ui.components.VisualBoard;
import com.chessbot.ui.components.VisualPiece;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;

// Order of a completed drag operation: dragDetected - dragEnter - dragExit - dragEnter - dragDropped - dragExit - dragDone
public class DragMove {
    private final VisualBoard visualBoard;
    private final Board board;
    private VisualPiece draggedPiece;
    private Square startingSquare;
    private Square endingSquare;
    private Square previousStartingSquare;
    private Square previousEndingSquare;
    private Square failedStartingSquare;
    private static final AudioClip moveSound = new AudioClip(ChessApplication.class.getResource("Sounds/move-self.mp3").toString());


    // Initializes a Board reference so the listeners can access its methods
    public DragMove(VisualBoard visualBoard) {
        this.visualBoard = visualBoard;
        this.board = visualBoard.getBoard();
    }


    // Triggers when a drag operation starts
    public void dragDetected(MouseEvent event) {
        // Square that the drag happened
        startingSquare = (Square) event.getSource();

        // If the square has no pieces, return
        if (startingSquare.getCurrentPiece() == null) {
            return;
        }

        // Resets selected color on drag operations that failed
        if (failedStartingSquare != null) {
            failedStartingSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
            failedStartingSquare.setIsSelected(true);
        }

        // Have to do it
        Dragboard db = startingSquare.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString("piece_move");
        db.setContent(content);

        // Sets the dragged piece background color to transparent
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        // The dragged piece might be reversed, if the player is black, so we set its rotation to 0 and after the snapshot
        // has been taken, put the original rotation back
        draggedPiece = startingSquare.getCurrentPiece();
        double originalRotation = draggedPiece.getRotate();
        draggedPiece.setRotate(0);
        Image pieceImage = draggedPiece.snapshot(params, null);
        draggedPiece.setRotate(originalRotation);

        // Sets the mouse to the middle of the dragged piece
        db.setDragView(pieceImage);
        db.setDragViewOffsetX(pieceImage.getWidth() / 2);
        db.setDragViewOffsetY(pieceImage.getHeight() / 2);
        startingSquare.setViewOrder(-1);

        // Makes the dragged piece invisible for the whole drag operation
        draggedPiece.setVisible(false);

        // If it's a light square give it a light selected color, else a dark one
        startingSquare.setStyle("-fx-background-color: #f5f682", "-fx-background-color: #b9ca43");
        startingSquare.setIsSelected(true);

        event.consume();
    }


    // Triggers when hovering a square while dragging
    public void dragOver(DragEvent event) {
        // Makes every square that the mouse hovers available for the rest of the dragging operation
        if (event.getGestureSource() != event.getSource() && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }

        event.consume();
    }


    // Triggers when hovering a new square while dragging
    public void dragEntered(DragEvent event) {
        // Adds a border effect to the square
        Square hoveredSquare = (Square) event.getSource();
        hoveredSquare.setStyle(hoveredSquare.getStyle() + "; -fx-border-color: #f8f8ef; -fx-border-width: 4; -fx-padding: -4;", hoveredSquare.getStyle() + "; -fx-border-color: #cedac3; -fx-border-width: 4; -fx-padding: -4;");

        event.consume();
    }


    // Triggers when letting off the drag operation
    public void dragDropped(DragEvent event) {
        if (draggedPiece.getColor() == board.getTurn()) { // If the dragged piece color matches the turn
            // Gets the square that the piece was dropped off
            endingSquare = (Square) event.getSource();

            // Creates and sends move to the engine, have to reverse back the bitboard square indexes because the JavaFX bitboard
            // is reversed (starts from the top left, instead of the bottom left)
            int startingSquareIndex = (7 - startingSquare.getRow()) * 8 + startingSquare.getCol();
            int endingSquareIndex = (7 - endingSquare.getRow()) * 8 + endingSquare.getCol();
            int move = Move.createMove(startingSquareIndex, endingSquareIndex, 0);
            board.makeMove(move);

            // Forces the UI to redraw based on the engine board
            visualBoard.sync();

            // Adds move sound
            moveSound.play();

            // Resets previous selected colors
            if (previousStartingSquare != null && previousEndingSquare != null) {
                previousStartingSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
                previousStartingSquare.setIsSelected(false);
                previousEndingSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
                previousEndingSquare.setIsSelected(false);
            }

            // Doesn't go to dragDone without it
            event.setDropCompleted(true);
            event.consume();
        }
    }


    // Triggers when exiting a square while dragging or after dropping a piece
    public void dragExited(DragEvent event) {
        Square hoveredSquare = (Square) event.getSource();

        // If exiting the starting square while dragging, it goes back to the selected color (removes the border)
        if (hoveredSquare == startingSquare) {
            startingSquare.setStyle("-fx-background-color: #f5f682", "-fx-background-color: #b9ca43");
            startingSquare.setIsSelected(true);

        }

        // If exiting a square while dragging, it goes back to the default color (removes the border)
        else if (hoveredSquare != endingSquare) {
            hoveredSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
        }

        // If dropping a piece and the ending square is a light square, give it a light selected color, else a dark one
        else {
            hoveredSquare.setStyle("-fx-background-color: #f5f682", "-fx-background-color: #b9ca43");
            hoveredSquare.setIsSelected(true);
        }

        event.consume();
    }


    // Final stage of a drag operation
    public void dragDone(DragEvent event) {
        Square startingSquare = (Square) event.getSource();

        // If drag completed, remove the hover effect and save variables for later
        if (event.getTransferMode() == TransferMode.MOVE) {
            startingSquare.setCursor(Cursor.DEFAULT);

            previousStartingSquare = startingSquare;
            previousEndingSquare = endingSquare;
        }

        // If drag failed (drop in the starting square or drop out of bounds) make the piece visible again and save variables
        // for later
        else {
            draggedPiece.setVisible(true);
            failedStartingSquare = startingSquare;
        }

        event.consume();
    }
}
