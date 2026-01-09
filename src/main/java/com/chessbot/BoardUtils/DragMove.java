package com.chessbot.BoardUtils;

import com.chessbot.ChessApplication;
import com.chessbot.Objects.Board;
import com.chessbot.Objects.Piece;
import com.chessbot.Objects.Square;
import com.chessbot.ViewManager;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.media.AudioClip;

// Order of a completed drag operation: dragDetected - dragEnter - dragExit - dragEnter - dragDropped - dragExit - dragDone
public class DragMove {
    private final Board board;
    private Piece draggedPiece;
    private Square startingSquare;
    private Square endingSquare;
    private Square previousStartingSquare;
    private Square previousEndingSquare;
    private Square failedStartingSquare;


    // Initializes a Board reference so the listeners can access the Board methods
    public DragMove(Board board) {
        this.board = board;
    }


    // Triggers when a drag operation starts
    public void dragDetected(MouseEvent event) {
        // Square that the drag happened
        startingSquare = (Square) event.getSource();

        // If the square has no pieces, return
        if (startingSquare.getCurrentPiece() == null) {
            return;
        }

        // Resets selected colour on drag operations that failed
        if (failedStartingSquare != null) {
            failedStartingSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
            failedStartingSquare.setIsSelected(true);
        }

        // Have to do it
        Dragboard db = startingSquare.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString("piece_move");
        db.setContent(content);

        // Sets the dragged piece background colour to transparent
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        draggedPiece = startingSquare.getCurrentPiece();
        Image pieceImage = draggedPiece.snapshot(params, null);

        // Sets the mouse to the middle of the dragged piece
        db.setDragView(pieceImage);
        db.setDragViewOffsetX(pieceImage.getWidth() / 2);
        db.setDragViewOffsetY(pieceImage.getHeight() / 2);
        startingSquare.setViewOrder(-1);

        // Makes the dragged piece invisible for the whole drag operation
        draggedPiece.setVisible(false);

        // If it's a light square give it a light selected colour, else a dark one
        startingSquare.setStyle("-fx-background-color: #f5f682", "-fx-background-color: #b9ca43");
        startingSquare.setIsSelected(true);

        event.consume();
    }


    // Triggers when hovering a square while dragging
    public void drag(DragEvent event) {
        // Makes every square that the mouse hovers available for the rest of the dragging operation
        if (event.getGestureSource() != event.getSource() && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }

        event.consume();
    }


    // Triggers when hovering a new square while dragging
    public void dragEnter(DragEvent event) {
        // Adds a border effect to the square
        Square hoveredSquare = (Square) event.getSource();

        hoveredSquare.setStyle(hoveredSquare.getStyle() + "; -fx-border-color: #f8f8ef; -fx-border-width: 4; -fx-padding: -4;", hoveredSquare.getStyle() + "; -fx-border-color: #cedac3; -fx-border-width: 4; -fx-padding: -4;");

        event.consume();
    }


    // Triggers when letting off the drag operation
    public void dragDropped(DragEvent event) {
        // Gets the square that the piece was dropped off
        endingSquare = (Square) event.getSource();

        // Adds the piece to ending square and makes it visible again
        endingSquare.setCurrentPiece(draggedPiece);
        draggedPiece.setVisible(true);

        // Makes the ending square hoverable
        endingSquare.setCursor(Cursor.HAND);

        // Adds move sound
        AudioClip clickSound = new AudioClip(ChessApplication.class.getResource("Sounds/move-self.mp3").toString());
        clickSound.play();

        // Resets previous selected colours
        if (previousStartingSquare != null && previousEndingSquare != null) {
            previousStartingSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
            previousStartingSquare.setIsSelected(false);
            previousEndingSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
            previousEndingSquare.setIsSelected(false);
        }

        // Doesn't go to dragDone without it
        event.setDropCompleted(true);

        event.consume();

        int bitboardIndex = 6 * draggedPiece.getColour() + draggedPiece.getPieceType(); // See Board bitboards to understand
        long bitboard = board.getBitboard(bitboardIndex);

        // Adds an 1 to the 64 bit long variable, the 1 is in the position of the piece
        // eg, piece in the 3rd row and 4th column = bit 27
        bitboard += 1L << (endingSquare.getRow() * 8L + endingSquare.getCol());
        bitboard -= 1L << (startingSquare.getRow() * 8L + startingSquare.getCol());
        board.setBitboard(bitboardIndex, bitboard);

        ViewManager.instance.callBitboardVisualization();
    }


    // Triggers when exiting a square while dragging or after dropping a piece
    public void dragExit(DragEvent event) {
        Square hoveredSquare = (Square) event.getSource();

        // If exiting the starting square while dragging, goes back to the selected colour (removes the border)
        if (hoveredSquare == startingSquare) {
            startingSquare.setStyle("-fx-background-color: #f5f682", "-fx-background-color: #b9ca43");
            startingSquare.setIsSelected(true);

        }

        // If exiting a square while dragging, goes back to the default colour (removes the border)
        else if (hoveredSquare != endingSquare) {
            hoveredSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
        }

        // If dropping a piece, and the ending square is a light square give it a light selected colour, else a dark one
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

        // If drag failed, drop in the starting square or drop out of bounds, make the piece visible again and save
        // variables for later
        else {
            draggedPiece.setVisible(true);

            failedStartingSquare = startingSquare;
        }

        event.consume();
    }
}
