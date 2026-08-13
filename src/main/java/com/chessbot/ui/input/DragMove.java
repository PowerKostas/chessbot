package com.chessbot.ui.input;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.ui.components.Square;
import com.chessbot.ui.components.VisualBoard;
import com.chessbot.ui.components.VisualPiece;
import com.chessbot.ui.utils.SoundManager;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.input.*;
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

    // Initializes a Board reference so the listeners can access its methods
    public DragMove(VisualBoard visualBoard) {
        this.visualBoard = visualBoard;
        this.board = visualBoard.getBoard();
    }


    // Loops through all the legal moves to see if any move's starting and ending squares match the drag's starting and
    // ending squares
    private int getLegalMoveFromDrag(Square endingSquare) {
        // Have to reverse back the bitboard square indexes because the JavaFX bitboard is reversed (starts from the top
        // left, instead of the bottom left)
        int startingSquareIndex = (7 - startingSquare.getRow()) * 8 + startingSquare.getCol();
        int endingSquareIndex = (7 - endingSquare.getRow()) * 8 + endingSquare.getCol();

        for (int i = 0; i < board.getLegalMoveCount(); i += 1) {
            int legalMove = board.getLegalMove(i);
            if (Move.getStartingSquare(legalMove) == startingSquareIndex && Move.getEndingSquare(legalMove) == endingSquareIndex) {
                return legalMove;
            }
        }

        return -1;
    }


    // Triggers when a drag operation starts
    public void dragDetected(MouseEvent event) {
        // Square that the drag happened
        startingSquare = (Square) event.getSource();

        // Initializes the square that the piece will get dropped on
        endingSquare = startingSquare;

        // If the square has no pieces, return
        if (startingSquare.getCurrentPiece() == null) {
            return;
        }

        // Have to do it
        Dragboard db = startingSquare.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString("piece_move");
        db.setContent(content);

        // Sets the dragged piece background color to transparent
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        // If the player is black. the dragged piece might be reversed, so we set its rotation to 0 and after the snapshot
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

        // Sets the square as selected in order for it to be colored appropriately
        startingSquare.setIsSelected(true);

        event.consume();
    }


    // Triggers when hovering a square while dragging
    public void dragOver(DragEvent event) {
        // Allows the piece to be dropped on any hovered square that contains a legal move for that piece
        if (event.getDragboard().hasString()) {
            Square hoveredSquare = (Square) event.getSource();

            if (getLegalMoveFromDrag(hoveredSquare) != -1) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        }

        event.consume();
    }


    // Triggers when hovering a new square while dragging
    public void dragEntered(DragEvent event) {
        // Adds a border effect to the square
        Square hoveredSquare = (Square) event.getSource();
        hoveredSquare.setColor(hoveredSquare.getStyle() + "; -fx-border-color: #f8f8ef; -fx-border-width: 4; -fx-padding: -4;", hoveredSquare.getStyle() + "; -fx-border-color: #cedac3; -fx-border-width: 4; -fx-padding: -4;");

        // Ending square needs the square that the mouse is currently hovering in order to play the illegal sound, because the
        // code will never go to dragDropped when playing an illegal move
        endingSquare = hoveredSquare;

        event.consume();
    }


    // Triggers when letting off the drag operation
    public void dragDropped(DragEvent event) {
        // Gets the square that the piece was dropped off
        endingSquare = (Square) event.getSource();

        // Makes the legal move
        int legalMove = getLegalMoveFromDrag(endingSquare);
        board.makeMove(legalMove);

        // Forces the UI to redraw based on the engine board
        visualBoard.sync();

        // Plays the appropriate move sound
        SoundManager.playMoveSound(board.getInCheck(), Move.getFlag(legalMove));

        // Resets the colors of the previous move squares
        if (previousStartingSquare != null && previousEndingSquare != null) {
            previousStartingSquare.setIsPreviousMove(false);
            previousEndingSquare.setIsPreviousMove(false);
        }

        // Sets new previous move squares
        startingSquare.setIsSelected(false);
        startingSquare.setIsPreviousMove(true);
        endingSquare.setIsSelected(false);
        endingSquare.setIsPreviousMove(true);

        // Tells dragDone that the drag was successful
        event.setDropCompleted(true);

        event.consume();
    }


    // Triggers when exiting a square while dragging or after dropping a piece
    public void dragExited(DragEvent event) {
        Square hoveredSquare = (Square) event.getSource();
        hoveredSquare.updateColor();

        event.consume();
    }


    // Final stage of a drag operation
    public void dragDone(DragEvent event) {
        Square startingSquare = (Square) event.getSource();

        // If the drag was successful, remove the hover effect and save variables for later
        if (event.getTransferMode() == TransferMode.MOVE) {
            startingSquare.setCursor(Cursor.DEFAULT);
            previousStartingSquare = startingSquare;
            previousEndingSquare = endingSquare;
        }

        // If the drag was unsuccessful (illegal move, drop in the starting square or drop out of bounds), make the piece
        // visible again
        else {
            draggedPiece.setVisible(true);

            // Doesn't play the illegal sound if a piece was dropped in it's starting square
            if (startingSquare != endingSquare) {
                SoundManager.playIllegalSound();
            }
        }

        event.consume();
    }
}
