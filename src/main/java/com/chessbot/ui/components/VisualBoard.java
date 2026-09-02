package com.chessbot.ui.components;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.movegen.MoveList;
import com.chessbot.ui.input.MoveHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

public class VisualBoard extends StackPane {
    // VisualBoard extends StackPane in order to center the promotion dialog inside the board, the actual board grid is just
    // a child inside the StackPane
    private final GridPane boardGrid;

    // 0 = White's pieces first, 1 = Black's pieces first. In the engine board the first square is a1, in the visual board the
    // first square is h8, if the player is black the first squares remain the same, but the visual board is flipped
    private final int boardPerspective;

    // The squares that will be highlighted when a move is made
    private Square previousStartingSquare;
    private Square previousEndingSquare;

    // Flag to determine if this board is gonna show debug visuals
    private final boolean isDebugBoard;

    // Flag to not allow human moves when the AI is thinking
    private boolean isBoardLocked = false;

    private final Board board;


    public VisualBoard(int boardPerspective, String fen, boolean isDebugBoard) {
        this.boardGrid = new GridPane();
        this.boardPerspective = boardPerspective;
        this.isDebugBoard = isDebugBoard;
        this.board = new Board();

        // Reverse the board, if the player is black
        if (boardPerspective == Piece.BLACK) {
            this.setRotate(180);
        }

        // Creates the 8x8 grid
        this.setPrefSize(700, 700);

        for (int i = 0; i < 8; i++) {
            ColumnConstraints colConstraint = new ColumnConstraints();
            colConstraint.setPercentWidth(12.5);
            boardGrid.getColumnConstraints().add(colConstraint);
        }

        for (int i = 0; i < 8; i++) {
            RowConstraints rowConstraint = new RowConstraints();
            rowConstraint.setPercentHeight(12.5);
            boardGrid.getRowConstraints().add(rowConstraint);
        }

        // Adds every custom square to the board grid, the square will automatically get scaled
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                Square square = new Square(row, col, this);
                boardGrid.add(square, col, row);
            }
        }

        this.getChildren().add(boardGrid);

        // Loads the initial position on the engine and visual boards
        board.loadInitialPosition(fen);
        sync(null); // Passing null because legal moves haven't been generated yet

        // Listens for clicks inside the board, on a left click, reset the right-clicked squares
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        Square square = (Square) boardGrid.getChildren().get(row * 8 + col);
                        if (square.getIsRightClicked()) {
                            square.setIsRightClicked(false);
                        }
                    }
                }
            }
        });
    }


    public Board getBoard() { return board; }

    public int getBoardPerspective() { return boardPerspective; }

    public boolean getIsBoardLocked() { return isBoardLocked; }

    public void setIsBoardLocked(boolean isBoardLocked) { this.isBoardLocked = isBoardLocked; }


    // Makes every square draggable/clickable, more info on the specific classes. The reason this is an external function, and
    // it's not inside the constructor is because MoveHandler's constructor needs a callback and VisualBoard shouldn't know
    // about that
    public void attachMoveHandler(MoveHandler moveHandler) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Square square = (Square) boardGrid.getChildren().get(row * 8 + col);

                square.setOnDragDetected(moveHandler::dragDetected);
                square.setOnDragOver(moveHandler::dragOver);
                square.setOnDragEntered(moveHandler::dragEntered);
                square.setOnDragExited(moveHandler::dragExited);
                square.setOnDragDropped(moveHandler::dragDropped);
                square.setOnDragDone(moveHandler::dragDone);
                square.setOnMouseReleased(moveHandler::mouseReleased);
            }
        }

        // Listens for clicks inside the board, on a right click, cancel the selected square
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                moveHandler.cancelSelection();
            }
        });
    }


    // Retrieves a Square object from the GridPane using a bitboard index
    public Square getSquare(int squareIndex) {
        int targetRow = 7 - (squareIndex / 8);
        int targetCol = squareIndex % 8;
        return (Square) boardGrid.getChildren().get(targetRow * 8 + targetCol);
    }


    // Works in a similar way to Board.searchLegalMove, but this function is for the UI only because it's used to find the
    // legal move that promotes the pawn to the piece that the user has selected
    public int searchPromotionLegalMove(MoveList moveList, int startingSquare, int endingSquare, int chosenPiece) {
        for (int i = 0; i < moveList.count; i += 1) {
            int legalMove = moveList.moves[i];
            if (Move.getStartingSquare(legalMove) == startingSquare && Move.getEndingSquare(legalMove) == endingSquare) {
                int moveFlag = Move.getFlag(legalMove);

                // Verifies that it's a promotion or a promotion capture move flag and that the move flag special bits match
                // the piece that the user has selected to promote to
                if (moveFlag >= Move.FLAG_KNIGHT_PROMOTION && ((moveFlag & 3) + 1) == chosenPiece) {
                    return legalMove;
                }
            }
        }

        return -1;
    }


    // Syncs the visual board to the engine board, runs after every move
    public void sync(MoveList moveList) {
        // For every square
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                int squareIndex = (7 - row) * 8 + col;
                int pieceColor = board.getPieceColorAtSquare(squareIndex);
                int pieceType = board.getPieceTypeAtSquare(squareIndex);
                Square square = (Square) boardGrid.getChildren().get(row * 8 + col);
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

                        VisualPiece newPiece = new VisualPiece(pieceColor, pieceType, this.boardPerspective == Piece.BLACK);
                        square.setCurrentPiece(newPiece);
                    }
                }
            }
        }

        // If this is a debug board, create a legal moves bitboard to visualize
        if (this.isDebugBoard) {
            long legalMovesBitboard = 0L;

            for (int i = 0; i < moveList.count; i += 1) {
                int legalMove = moveList.moves[i];
                int endingSquare = Move.getEndingSquare(legalMove);
                legalMovesBitboard |= 1L << endingSquare;
            }

            bitboardVisualization(legalMovesBitboard);
        }
    }


    public void highlightPreviousMove(int startingSquareIndex, int endingSquareIndex) {
        // Resets the colors of the previous move squares
        if (previousStartingSquare != null && previousEndingSquare != null) {
            previousStartingSquare.setIsPreviousMove(false);
            previousEndingSquare.setIsPreviousMove(false);
        }

        // Sets new previous move squares and highlights them
        previousStartingSquare = getSquare(startingSquareIndex);
        previousEndingSquare = getSquare(endingSquareIndex);

        if (previousStartingSquare != null && previousEndingSquare != null) {
            previousStartingSquare.setIsPreviousMove(true);
            previousEndingSquare.setIsPreviousMove(true);
        }
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
                    Square square = (Square) boardGrid.getChildren().get(row * 8 + col);
                    boolean hasPiece = this.board.getPieceColorAtSquare(squareIndex) != -1;
                    square.updateLegalHint(!hasPiece, hasPiece);
                }
            }
        }
    }


    public void clearLegalHints() {
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                Square square = (Square) boardGrid.getChildren().get(row * 8 + col);
                square.updateLegalHint(false, false);
            }
        }
    }


    // Iterates through each bit of the bitboard that was given, if the bit equals 1, the corresponding square gets painted
    // red, all other squares get the default colors
    public void bitboardVisualization(long bitboard) {
        // The engine board starts from a1, but the JavaFX board starts from a8, so the bitboard has to be reversed
        bitboard = Long.reverseBytes(bitboard);

        for (int i = 0; i < 64; i += 1) {
            Square square = (Square) boardGrid.getChildren().get(i);
            long squareMask = 1L << i;

            if ((bitboard & squareMask) != 0) {
                square.setStyle("-fx-background-color: red");
            }

            else {
                if ((square.getRow() + square.getCol()) % 2 == 0) { // If light square
                    square.setStyle("-fx-background-color: #ebecd0");
                }

                else { // If dark square
                    square.setStyle("-fx-background-color: #739552");
                }
            }
        }
    }
}
