package com.chessbot.ui.input;

import com.chessbot.application.GameManager;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;
import com.chessbot.ui.components.PromotionDialog;
import com.chessbot.ui.components.Square;
import com.chessbot.ui.components.VisualBoard;
import com.chessbot.ui.components.VisualPiece;
import com.chessbot.ui.utils.SoundManager;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

// This class is responsible for handling input in the visual board and translating it for the engine board
public class MoveHandler {
    private final VisualBoard visualBoard;
    private final Board board;
    private final GameManager gameManager;
    private Square startingSquare;
    private int startingSquareIndex; // The starting square index is used multiple times, a class variable is utilized for efficiency

    // Drag Move specific variables
    private VisualPiece draggedPiece;
    private Square endingSquare;


    // Initializes the references in order for the listeners to access their methods
    public MoveHandler(VisualBoard visualBoard, GameManager gameManager) {
        this.visualBoard = visualBoard;
        this.board = visualBoard.getBoard();
        this.gameManager = gameManager;
    }


    // Colors a square, and shows the legal moves hints for any piece in that square
    private void selectPiece(Square square, int squareIndex) {
        startingSquare = square;
        startingSquareIndex = squareIndex;
        startingSquare.setIsSelected(true);
        long pieceLegalMovesBitboard = gameManager.getMoveList().searchPieceLegalMoves(startingSquareIndex);
        visualBoard.showLegalHints(pieceLegalMovesBitboard);
    }


    // Clears the square's color, the starting square and any legal hints
    public void cancelSelection() {
        if (startingSquare != null) { // When right-clicking an empty square, startingSquare is null, have to check against that
            startingSquare.setIsSelected(false);
            startingSquare = null;
            visualBoard.clearLegalHints();
        }
    }


    private void promotePawn(Square promotionSquare, int promotionSquareIndex) {
        int pieceColor = board.getPieceColorAtSquare(startingSquareIndex);

        // Opens the promotion dialog and waits for the user to select a piece
        PromotionDialog.display(visualBoard, pieceColor, visualBoard.getBoardPerspective() == Piece.BLACK, chosenPiece -> {
            // If the user didn't click the 'x' button, execute the promotion legal move
            if (chosenPiece != -1) {
                int legalMove = visualBoard.searchPromotionLegalMove(gameManager.getMoveList(), startingSquareIndex, promotionSquareIndex, chosenPiece);
                gameManager.playMove(legalMove);
                cancelSelection();
            }

            // Else, make the dragged/clicked piece visible again, cancel the selection and remove the target square's selected color
            else {
                draggedPiece.setVisible(true);
                cancelSelection();
                promotionSquare.updateColor();
            }
        });
    }


    // Drag Move
    // Triggers when a drag operation starts
    public void dragDetected(MouseEvent event) {
        // Blocks human input if it's the AI's turn
        if (visualBoard.getIsBoardLocked()) return;

        Square dragSource = (Square) event.getSource();

        // If the square has no pieces, return
        if (dragSource.getCurrentPiece() == null) return;

        // If a piece is already selected from a click and the user drags a different piece, cancel the old selection
        if (this.startingSquare != null && this.startingSquare != dragSource) {
            cancelSelection();
        }

        // Have to reverse back the bitboard square indexes because the JavaFX bitboard is reversed (starts from the top
        // left, instead of the bottom left)
        startingSquareIndex = (7 - dragSource.getRow()) * 8 + dragSource.getCol();
        selectPiece(dragSource, startingSquareIndex);

        // Initializes the square that the piece will get dropped on
        endingSquare = startingSquare;

        // Have to do it
        Dragboard db = startingSquare.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString("piece_move");
        db.setContent(content);

        // Sets the dragged piece background color to transparent
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        // Matches the physical pixel density of the display so the drag ghost isn't rendered smaller than the actual piece
        double scaleX = startingSquare.getScene().getWindow().getOutputScaleX();
        double scaleY = startingSquare.getScene().getWindow().getOutputScaleY();
        params.setTransform(new Scale(scaleX, scaleY));

        // If the player is black, the dragged piece is reversed, so we set its rotation to 0
        draggedPiece = startingSquare.getCurrentPiece();
        double originalRotation = draggedPiece.getRotate();
        draggedPiece.setRotate(0);

        // Takes a snapshot
        Bounds pieceBounds = draggedPiece.getLayoutBounds();
        WritableImage buffer = new WritableImage(
                Math.max(1, (int) Math.round(pieceBounds.getWidth() * scaleX)),
                Math.max(1, (int) Math.round(pieceBounds.getHeight() * scaleY))
        );

        // Puts the original rotation back
        Image pieceImage = draggedPiece.snapshot(params, buffer);
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
            int hoveredSquareIndex = (7 - hoveredSquare.getRow()) * 8 + hoveredSquare.getCol();

            if (gameManager.getMoveList().searchLegalMove(startingSquareIndex, hoveredSquareIndex) != -1) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        }

        event.consume();
    }


    // Triggers when hovering a new square while dragging
    public void dragEntered(DragEvent event) {
        // Adds a border effect to the square
        Square hoveredSquare = (Square) event.getSource();
        hoveredSquare.setStyle(hoveredSquare.getStyle() + "; -fx-border-color: #f8f8ef; -fx-border-width: 4; -fx-padding: -4;", hoveredSquare.getStyle() + "; -fx-border-color: #cedac3; -fx-border-width: 4; -fx-padding: -4;");

        // Ending square needs the square that the piece is currently hovering in order to play, if needed, the illegal sound, in
        // dragDone. The code will never go to dragDropped when playing an illegal move which is where endingSquare normally
        // gets its value
        endingSquare = hoveredSquare;

        event.consume();
    }


    // Triggers when letting off the drag operation
    public void dragDropped(DragEvent event) {
        // Sets the legal square that the piece was dropped off
        endingSquare = (Square) event.getSource();

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
        Square dragSource = (Square) event.getSource();

        // If the drag was successful
        if (event.getTransferMode() == TransferMode.MOVE) {
            dragSource.setCursor(Cursor.DEFAULT);

            int endingSquareIndex = (7 - endingSquare.getRow()) * 8 + endingSquare.getCol();
            int pieceType = board.getPieceTypeAtSquare(startingSquareIndex);

            // If the dragged piece is a pawn and its ending square is in the final rank, it's a promotion and handled separately
            if (pieceType == Piece.PAWN && (endingSquareIndex <= 7 || endingSquareIndex >= 56)) {
                promotePawn(endingSquare, endingSquareIndex);
            }

            // Executes normal legal moves
            else {
                int legalMove = gameManager.getMoveList().searchLegalMove(startingSquareIndex, endingSquareIndex);
                gameManager.playMove(legalMove);
                cancelSelection();
            }
        }

        // If the drag was unsuccessful (illegal move, drop in the starting square or drop out of bounds)
        else {
            // Makes the dragged piece visible again
            draggedPiece.setVisible(true);

            // Plays the illegal sound if the piece wasn't dropped in the starting square
            if (dragSource != endingSquare) {
                SoundManager.playIllegalSound();
            }
        }

        event.consume();
    }


    // Click Move
    // Triggers when a square is clicked
    public void mouseReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return; // Only left clicks are accepted
        if (visualBoard.getIsBoardLocked()) return;

        Square clickedSquare = (Square) event.getSource();
        int clickedSquareIndex = (7 - clickedSquare.getRow()) * 8 + clickedSquare.getCol();

        // If no piece is currently selected (user just clicked a square) and the square has a piece, select the piece
        if (startingSquare == null && clickedSquare.getCurrentPiece() != null) {
            selectPiece(clickedSquare, clickedSquareIndex);
        }

        // If the user has already clicked a square and it wasn't empty
        else if (startingSquare != null) {
            // If the starting square was clicked again, cancel the selection
            if (startingSquare == clickedSquare ) {
                cancelSelection();
            }

            // For any other square click
            else {
                int pieceType = board.getPieceTypeAtSquare(startingSquareIndex);

                // If the clicked piece is a pawn, its ending square is in the final rank and the move the user is trying to
                // do is legal, it's a promotion and handled separately
                if (pieceType == Piece.PAWN &&
                    (clickedSquareIndex <= 7 || clickedSquareIndex >= 56) &&
                        gameManager.getMoveList().searchLegalMove(startingSquareIndex, clickedSquareIndex) != -1)
                {
                    // Hides the pawn while the promotion dialog is open
                    startingSquare.getCurrentPiece().setVisible(false);

                    promotePawn(clickedSquare, clickedSquareIndex);
                    event.consume();
                }

                // Executes normal legal moves
                else {
                    int legalMove = gameManager.getMoveList().searchLegalMove(startingSquareIndex, clickedSquareIndex);

                    // If the click is a legal move
                    if (legalMove != -1) {
                        gameManager.playMove(legalMove);
                        cancelSelection();
                    }

                    else {
                        // If the user clicked a square with a piece on it, cancel the current selection and switch the selection
                        // to the new clicked piece
                        if (clickedSquare.getCurrentPiece() != null) {
                            cancelSelection();
                            selectPiece(clickedSquare, clickedSquareIndex);
                        }

                        // If it's just an illegal move
                        else {
                            SoundManager.playIllegalSound();
                            cancelSelection();
                        }
                    }
                }
            }
        }

        event.consume();
    }
}
