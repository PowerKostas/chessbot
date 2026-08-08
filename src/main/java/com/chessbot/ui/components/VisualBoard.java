package com.chessbot.ui.components;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.ui.controllers.MainController;
import com.chessbot.ui.input.DragMove;
import com.chessbot.ui.input.RightClick;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class VisualBoard extends GridPane {
    // Holds a reference to the engine board
    private final Board board;

    // 0 = White, 1 = Black. In the engine board the first square is a1, in the visual board the first square
    // is h8, if the player is black the first squares remain the same, but the visual board is flipped
    private int playerColor;


    public VisualBoard(int playerColor, String fen) {
        this.board = new Board();
        this.playerColor = playerColor;

        // If the player is black, reverse the board
        if (playerColor == 1) {
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
        DragMove dragMove = new DragMove(this);
        RightClick rightClick = new RightClick();

        // Prepares every custom square class for the JavaFX board
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                Square square = new Square(row, col, this);

                // Makes every square draggable/clickable, more info on the specific classes
                square.setOnDragDetected(dragMove::dragDetected);
                square.setOnDragOver(dragMove::drag);
                square.setOnDragEntered(dragMove::dragEnter);
                square.setOnDragExited(dragMove::dragExit);
                square.setOnDragDropped(dragMove::dragDropped);
                square.setOnDragDone(dragMove::dragDone);

                square.setOnMouseClicked(rightClick::click);

                this.add(square, col, row);
            }
        }

        // Sets pieces on the board
        this.board.loadPosition(fen);
        this.sync();
    }


    // Syncs the visual board to the engine board
    public void sync() {
        // For every square
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                int squareIndex = (7 - row) * 8 + col;
                int[] pieceInfo = board.getPieceAtSquare(squareIndex);
                Square square = (Square) this.getChildren().get(row * 8 + col);
                VisualPiece visualPiece = square.getCurrentPiece();

                // If the engine square is empty and the visual square isn't, clear the visual square
                if (pieceInfo == null) {
                    if (visualPiece != null) {
                        square.getChildren().remove(visualPiece);
                        square.setCurrentPiece(null);
                    }
                }

                // If the engine square isn't empty and the visual square is or has different data, set the piece from
                // the engine to the visual square
                else {
                    int pieceColor = pieceInfo[0];
                    int pieceType = pieceInfo[1];

                    boolean needsUpdate = visualPiece == null || visualPiece.getColor() != pieceColor || visualPiece.getType() != pieceType;
                    if (needsUpdate) {
                        if (visualPiece != null) {
                            square.getChildren().remove(visualPiece);
                        }

                        VisualPiece newPiece = new VisualPiece(pieceColor, pieceType, this.playerColor == 1);
                        square.setCurrentPiece(newPiece);
                    }
                }
            }
        }

        // Creates a legal moves bitboard for UI debugging
        long legalMovesBitboard = 0L;

        for (int i = 0; i < this.board.getLegalMoveCount(); i += 1) {
            int legalMove = this.board.getLegalMove(i);
            int endingSquare = Move.getEndingSquare(legalMove);
            legalMovesBitboard |= 1L << endingSquare;
        }

        MainController.instance.bitboardVisualization(legalMovesBitboard);
    }


    public Board getBoard() { return board; }

    public int getPlayerColor() { return playerColor; }

    public void setPlayerColor(int playerColor) {
        this.playerColor = playerColor;
    }
}
