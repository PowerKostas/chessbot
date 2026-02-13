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

    private final long[] rookMovesLookupTable;

    // Stores the starting index of each square in the 1D rookMovesLookupTable array (for fancy magic bitboards)
    int[] rookOffsets = new int[64];

    // Precomputed
    private final long[] rookMagicNumbers = {36033748042518560L, 18031991769538564L, 4791838937096392704L, 72062060820701272L, 612509357724663936L, 2449975793838211584L, 72058225398907392L, 144119601156866306L, 4918071670163456004L, 1152992148497375232L, 2419699774745739392L, -9222105365098790784L, 9148487102892032L, 577868144368027904L, 4627589358914896384L, 18577430294036550L, -9151171505734663424L, 78252243102483584L, 2315009638191350017L, 577879123142131744L, 297520150239248400L, -7959548841332563328L, 1152925902941718546L, 27340456142602516L, 2316046479269199874L, 282029029605376L, 576495940974739733L, 1424969217605764L, 4611765185412333696L, 562958544406532L, 180146338737948673L, 1152922750147527236L, 1153414087971962920L, 35253095759936L, -8025271461998489600L, 9900453269504L, -6760888185913341952L, 1229764208732668928L, 18578516761118730L, 4762838134759097410L, 306280512010485760L, 4611721255681277952L, 5188428521122889745L, 4616207210391863392L, 1152981024265076752L, 37155315938754824L, 1196268685717798917L, -9150997232587767804L, -9222210402815832960L, 653303585227968768L, 4710765485647790336L, 2378041924873355392L, 3396700672623872L, 27025997454934144L, 326587943228539904L, 328904764425355776L, 2306480867733667921L, -6917237312388561823L, -9209710604581855231L, 5102835880428809L, 45598963944917002L, 3026981934175896850L, 8938104816132L, 281483569807489L};
    private final int[] rookBestBits = {12, 11, 11, 11, 11, 11, 11, 12, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 11, 10, 10, 10, 10, 10, 10, 11, 12, 11, 11, 11, 11, 11, 11, 12};

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

        // Calculates rookOffsets based on rookBestBits
        int currentOffset = 0;
        for (int i = 0; i < 64; i += 1) {
            rookOffsets[i] = currentOffset;
            currentOffset += (1 << rookBestBits[i]);
        }

        // Precomputes rookMovesLookupTable
        RookMagicBitboards magicBitboards = new RookMagicBitboards();
        rookMovesLookupTable = magicBitboards.createRookMovesLookupTable(rookMagicNumbers, rookBestBits, rookOffsets);

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

    public long getRookMoves(int offset, int magicIndex) {
        return rookMovesLookupTable[offset + magicIndex];
    }

    public int getRookOffsets(int index) {
        return rookOffsets[index];
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
