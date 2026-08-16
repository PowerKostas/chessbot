package com.chessbot.ui.components;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Piece;
import com.chessbot.ui.controllers.MainController;
import com.chessbot.ui.input.MoveHandler;
import com.chessbot.ui.input.RightClick;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class VisualBoard extends GridPane {
    // Holds a reference to the engine board
    private final Board board;

    // 0 = White, 1 = Black. In the engine board the first square is a1, in the visual board the first square is h8, if the
    // player is black the first squares remain the same, but the visual board is flipped
    private int playerColor;


    public VisualBoard(int playerColor, String fen) {
        this.board = new Board();
        this.playerColor = playerColor;

        // Reverse the board, if the player is black
        if (playerColor == Piece.BLACK) {
            this.setRotate(180);
        }

        // Sets up FXML
        this.setPrefSize(700, 700);

        for (int i = 0; i < 8; i++) {
            ColumnConstraints colConstraint = new ColumnConstraints();
            colConstraint.setPercentWidth(12.5);
            this.getColumnConstraints().add(colConstraint);
        }

        for (int i = 0; i < 8; i++) {
            RowConstraints rowConstraint = new RowConstraints();
            rowConstraint.setPercentHeight(12.5);
            this.getRowConstraints().add(rowConstraint);
        }

        // One instance of the classes for every board
        MoveHandler moveHandler = new MoveHandler(this);
        RightClick rightClick = new RightClick();

        // Prepares every custom square class for the JavaFX board
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                Square square = new Square(row, col, this);

                // Makes every square draggable/clickable, more info on the specific classes
                square.setOnDragDetected(moveHandler::dragDetected);
                square.setOnDragOver(moveHandler::dragOver);
                square.setOnDragEntered(moveHandler::dragEntered);
                square.setOnDragExited(moveHandler::dragExited);
                square.setOnDragDropped(moveHandler::dragDropped);
                square.setOnDragDone(moveHandler::dragDone);
                square.setOnMouseReleased(moveHandler::mouseReleased);

                square.setOnMouseClicked(rightClick::mouseClicked);

                this.add(square, col, row);
            }
        }

        // Sets pieces on the board
        this.board.loadPosition(fen);
        this.sync();

        // Listens for clicks inside the board
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // On a right click, cancel the selected square
            if (event.getButton() == MouseButton.SECONDARY) {
                moveHandler.cancelSelection();
            }

            // On a left click, reset the right-clicked squares
            if (event.getButton() == MouseButton.PRIMARY) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        Square square = (Square) this.getChildren().get(row * 8 + col);
                        if (square.getIsRightClicked()) {
                            square.setIsRightClicked(false);
                        }
                    }
                }
            }
        });
    }


    public Board getBoard() { return board; }

    public int getPlayerColor() { return playerColor; }

    public void setPlayerColor(int playerColor) {
        this.playerColor = playerColor;
    }


    // Syncs the visual board to the engine board
    public void sync() {
        // For every square
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                int squareIndex = (7 - row) * 8 + col;
                int pieceColor = board.getPieceColorAtSquare(squareIndex);
                int pieceType = board.getPieceTypeAtSquare(squareIndex);
                Square square = (Square) this.getChildren().get(row * 8 + col);
                VisualPiece visualPiece = square.getCurrentPiece();

                // If the engine square is empty and the visual square isn't, clear the visual square
                if (pieceColor == -1) {
                    if (visualPiece != null) {
                        square.getChildren().remove(visualPiece);
                        square.setCurrentPiece(null);
                    }
                }

                // If the engine square isn't empty and the visual square is or has different data, set the piece from the engine
                // to the visual square
                else {
                    boolean needsUpdate = visualPiece == null || visualPiece.getColor() != pieceColor || visualPiece.getType() != pieceType;
                    if (needsUpdate) {
                        if (visualPiece != null) {
                            square.getChildren().remove(visualPiece);
                        }

                        VisualPiece newPiece = new VisualPiece(pieceColor, pieceType, this.playerColor == Piece.BLACK);
                        square.setCurrentPiece(newPiece);
                    }
                }
            }
        }

        // Creates a legal moves bitboard for UI debugging
        long legalMovesBitboard = 0L;

        for (int i = 0; i < this.board.getLegalMovesCount(); i += 1) {
            int legalMove = this.board.getLegalMove(i);
            int endingSquare = Move.getEndingSquare(legalMove);
            legalMovesBitboard |= 1L << endingSquare;
        }

        MainController.instance.bitboardVisualization(board.getAttackMapBitboard(board.getTurn() ^ 1));
    }


    // Translates the piece's legal moves bitboard into UI legal move/capture hints
    public void showLegalHints(long pieceLegalMovesBitboard) {
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                int squareIndex = (7 - row) * 8 + col;

                // If the pieceLegalMovesBitboard bit is 1 at this square index
                boolean isLegal = (pieceLegalMovesBitboard & (1L << squareIndex)) != 0;

                // Updates the square's legal hint if there is a legal move/capture there. If there is not a piece, it's a
                // normal move, if there is, it's a capture
                if (isLegal) {
                    Square square = (Square) this.getChildren().get(row * 8 + col);
                    boolean hasPiece = this.board.getPieceColorAtSquare(squareIndex) != -1;
                    square.updateLegalHint(!hasPiece, hasPiece);
                }
            }
        }
    }


    // Clears all the legal hints
    public void clearLegalHints() {
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                Square square = (Square) this.getChildren().get(row * 8 + col);
                square.updateLegalHint(false, false);
            }
        }
    }
}
