package com.chessbot.Objects;

import com.chessbot.BoardUtils.RightClick;
import com.chessbot.BoardUtils.DragMove;
import com.chessbot.BoardUtils.FenReader;
import com.chessbot.Objects.Pieces.Knight;
import com.chessbot.Objects.Pieces.Pawn;
import com.chessbot.Objects.Pieces.Rook;
import com.chessbot.ViewManager;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

// Board class that is used to represent the board in JavaFX and the board state
public class Board extends GridPane {
    // 0 = White, 1 = Black. In the representation board the first square is a1, in the visual board the first square
    // is h8, if the player is black the first squares remain the same, but the visual board is flipped
    private final int playerColour;

    // 0 = White, 1 = Black
    private int turn = 0;

    // 12 64 bit variables, one for each piece colour and piece type, first dimension is the Piece colour and second
    // dimension is the Piece type, each bit represents a piece on the board, used for board representation
    private final long[][] bitboards = new long[2][6];

    // 0 = White bitboard, 1 = Black bitboard, 2 = All bitboard
    private final long[] otherBitboards = new long [3];

    private long allPseudoLegalMovesBitboard = 0;

    // 1 for the square than an en passant capture can happen, 0 for everything else
    private long enPassantSquareBitboard = 0;


    public Board(int playerColour, String fen) {
        this.playerColour = playerColour;

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
        FenReader.build(fen, this);

        // If the player is black, reverse the board
        if (playerColour == 1) {
            this.setRotate(180);
        }

        // Precomputes magic bitboards
    }


    public int getPlayerColour() {
        return playerColour;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public long getBitboard(int colour, int pieceType) {
        return bitboards[colour][pieceType];
    }

    public void setBitboard(int colour, int pieceType, long bitboard) {
        this.bitboards[colour][pieceType] = bitboard;
    }

    public long getOtherBitboard(int index) {
        return otherBitboards[index];
    }

    public void setOtherBitboard(int index, long bitboard) {
        this.otherBitboards[index] = bitboard;
    }

    public void setEnPassantSquareBitboard(long enPassantSquareBitboard) {
        this.enPassantSquareBitboard = enPassantSquareBitboard;
    }


    // Pseudo legal moves are legal moves that don't check if their king is in check after they are played, the program
    // first generates pseudo legal moves to instantly get legal moves after. pseudoLegalMoves methods are static because
    // they concern all the knights, for example, not a single knight object
    public void generateOpponentPseudoLegalMoves(int opponentColour) {
        // Resets each turn
        allPseudoLegalMovesBitboard = 0;

        //allPseudoLegalMovesBitboard |= Pawn.pseudoLegalMoves[opponentColour].generate(bitboards[opponentColour][1], otherBitboards[2], otherBitboards[opponentColour ^ 1]);
        //allPseudoLegalMovesBitboard |= Knight.pseudoLegalMoves(getBitboard(opponentColour, 2), otherBitboards[opponentColour]);

        ViewManager.instance.bitboardVisualization(allPseudoLegalMovesBitboard);
    }
}
