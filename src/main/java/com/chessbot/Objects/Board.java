package com.chessbot.Objects;

import com.chessbot.BitboardUtils.RookMagicBitboards;
import com.chessbot.VisualBoardUtils.RightClick;
import com.chessbot.VisualBoardUtils.DragMove;
import com.chessbot.VisualBoardUtils.FenReader;
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

    private long allPseudoLegalMovesBitboard;

    private final long[][] rookMovesLookupTable;

    // Precomputed
    private final long[] rookMagicNumbers = {2498368794263616L, 1143634901073920L, 612016063512584L, 1134700329435264L, 1128103359324160L, 1135866412023824L, 35829707833344L, 35666658394625L, 4611826758067421184L, 36380778184048640L, 577023985727209472L, 282576904298496L, 181551394354892800L, 72339106796994560L, 1125927019348992L, 1837750127238873600L, 4899918595783458816L, 17867601870884L, -5908720510736138240L, 144117395693248512L, 2200367661064L, 5044033781813608448L, 554059300864L, 5244442317976305664L, 288230979598811136L, 720681494570336256L, 35188671447040L, 18019492353409030L, 1315051237230641154L, 4402342068224L, 36029072987422720L, 42949739520L, 216243153030610944L, -9223370928652517376L, 1152923773792452608L, -4035223065993404415L, 162135779961737216L, -6917526828450050048L, 69323511810L, 5188146775059334144L, -9151303443329974272L, -9223352243481083904L, 18287975596032L, 594475430556336136L, 594475863814307840L, 42950852608L, 36028831647399936L, 2594073542148620288L, -9223371487094766976L, 2305844248617943296L, 2305862811162526208L, 576461044966359552L, 2305844108998476032L, 2882303778713766144L, 68753556480L, 18577366733324800L, 2305852566598230016L, 36564270059520L, 106111730714624L, 17626617217408L, 1297111462023856640L, 5764608077240337408L, 72063678952047104L, -8637901608977365888L};
    private final int[] rookBestBits = {5, 5, 5, 5, 5, 5, 5, 5, 10, 9, 9, 9, 9, 9, 9, 10, 9, 8, 8, 8, 8, 8, 8, 9, 9, 8, 8, 8, 8, 8, 8, 9, 9, 8, 8, 8, 8, 8, 8, 9, 9, 8, 8, 8, 8, 8, 8, 9, 10, 9, 9, 9, 9, 9, 9, 10, 5, 5, 5, 5, 5, 5, 5, 5};

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

        // Precomputes magic numbers and best bits, commented out if it's already done
        //RookMagicBitboards rookMagicBitboards = new RookMagicBitboards();
        //rookMagicBitboards.findBestMagicNumbers();

        // Precomputes rookMovesLookupTable
        RookMagicBitboards magicBitboards = new RookMagicBitboards();
        rookMovesLookupTable = magicBitboards.createRookMovesLookupTable(rookMagicNumbers, rookBestBits);

        // Sets pieces on the board
        FenReader.build(fen, this);

        // If the player is black, reverse the board
        if (playerColour == 1) {
            this.setRotate(180);
        }
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

    public long getAllPseudoLegalMovesBitboard() {
        return allPseudoLegalMovesBitboard;
    }

    public void setAllPseudoLegalMovesBitboard(long allPseudoLegalMovesBitboard) {
        this.allPseudoLegalMovesBitboard = allPseudoLegalMovesBitboard;
    }

    public long getRookMoves(int square, int magicIndex) {
        return rookMovesLookupTable[square][magicIndex];
    }

    public long getRookMagicNumbers(int index) {
        return rookMagicNumbers[index];
    }

    public long getRookBestBits(int index) {
        return rookBestBits[index];
    }

    public void setEnPassantSquareBitboard(long enPassantSquareBitboard) {
        this.enPassantSquareBitboard = enPassantSquareBitboard;
    }
}
