package com.chessbot.Objects.Pieces;

import com.chessbot.Objects.Piece;
import com.chessbot.PieceUtils.PawnPseudoLegalMoves;

public class Pawn extends Piece {
    public Pawn(int colour, boolean reverse) { super(colour, 1, reverse); }


    // Because pawn pseudo legal moves generate differently based on colour, to avoid an if in the calculations, we put
    // the 2 functions in an array
    public static final PawnPseudoLegalMoves[] pseudoLegalMoves = new PawnPseudoLegalMoves[] {Pawn::whitePseudoLegalMoves, Pawn::blackPseudoLegalMoves};

    public static long whitePseudoLegalMoves(long pawnsBitboard, long allPiecesBitboard, long opponentBitboard, long enPassantSquareBitboard) {
        // 1. Moves the pawn up 1 square, if there are no pieces there
        // 2. If the piece ends on the third rank, it can move up 1 square again, if there are no pieces there
        // 3 and 5. If the piece is not in the A file and there is an opponent piece/en passant target up and left 1
        // square, it can move there
        // 4 and 6. If the piece is not in the H file and there is an opponent piece/en passant target up and right 1
        // square, it can move there
        long singlePush = (pawnsBitboard << 8) & ~allPiecesBitboard;
        long doublePush = ((singlePush & 0x0000000000FF0000L) << 8) & ~allPiecesBitboard;
        long attackWest = ((pawnsBitboard & ~0x0101010101010101L) << 7) & opponentBitboard;
        long attackEast = ((pawnsBitboard & ~0x8080808080808080L) << 9) & opponentBitboard;
        long epAttackWest = ((pawnsBitboard & ~0x0101010101010101L) << 7) & enPassantSquareBitboard;
        long epAttackEast = ((pawnsBitboard & ~0x8080808080808080L) << 9) & enPassantSquareBitboard;

        return singlePush | doublePush | attackWest | attackEast | epAttackWest | epAttackEast;
    }

    public static long blackPseudoLegalMoves(long pawnsBitboard, long allPiecesBitboard, long opponentBitboard, long enPassantSquareBitboard) {
        // 1. Moves the pawn up 1 square, if there are no pieces there
        // 2. If the piece ends on the sixth rank, it can move up 1 square again, if there are no pieces there
        // 3 and 5. If the piece is not in the H file and there is an opponent piece/en passant target up and left 1
        // square, it can move there
        // 4 and 6. If the piece is not in the A file and there is an opponent piece/en passant target up and right 1
        // square, it can move there
        long singlePush = (pawnsBitboard >> 8) & ~allPiecesBitboard;
        long doublePush = ((singlePush & 0x0000FF0000000000L) >> 8) & ~allPiecesBitboard;
        long attackWest = ((pawnsBitboard & ~0x0101010101010101L) >> 9) & opponentBitboard;
        long attackEast = ((pawnsBitboard & ~0x8080808080808080L) >> 7) & opponentBitboard;
        long epAttackWest = ((pawnsBitboard & ~0x0101010101010101L) >> 9) & enPassantSquareBitboard;
        long epAttackEast = ((pawnsBitboard & ~0x8080808080808080L) >> 7) & enPassantSquareBitboard;

        return singlePush | doublePush | attackWest | attackEast | epAttackWest | epAttackEast;
    }
}
