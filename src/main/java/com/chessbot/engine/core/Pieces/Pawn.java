package com.chessbot.engine.core.Pieces;

// Normal moves and en passant moves are separated because there are instances that en passant moves don't need to be generated
public final class Pawn {
    private Pawn() {}


    public static long pseudoLegalMoves(long pawnsBitboard, long allPiecesBitboard, long opponentBitboard, boolean isWhite) {
        long singlePush;
        long doublePush;
        long attackWest;
        long attackEast;

        if (isWhite) {
            // 1. Moves the pawn up 1 square, if there are no pieces there
            // 2. If the piece ends on the third rank, it can move up 1 square again, if there are no pieces there
            // 3. If the piece is not in the A file and there is an opponent piece up and left 1 square, it can move there
            // 4. If the piece is not in the H file and there is an opponent piece up and right square, it can move there
            singlePush = (pawnsBitboard << 8) & ~allPiecesBitboard;
            doublePush = ((singlePush & 0x0000000000FF0000L) << 8) & ~allPiecesBitboard;
            attackWest = ((pawnsBitboard & ~0x0101010101010101L) << 7) & opponentBitboard;
            attackEast = ((pawnsBitboard & ~0x8080808080808080L) << 9) & opponentBitboard;
        }

        else {
            // 1. Moves the pawn up 1 square, if there are no pieces there
            // 2. If the piece ends on the sixth rank, it can move up 1 square again, if there are no pieces there
            // 3. If the piece is not in the H file and there is an opponent piece up and left 1 square, it can move there
            // 4. If the piece is not in the A file and there is an opponent piece up and right 1 square, it can move there
            singlePush = (pawnsBitboard >> 8) & ~allPiecesBitboard;
            doublePush = ((singlePush & 0x0000FF0000000000L) >> 8) & ~allPiecesBitboard;
            attackWest = ((pawnsBitboard & ~0x0101010101010101L) >> 9) & opponentBitboard;
            attackEast = ((pawnsBitboard & ~0x8080808080808080L) >> 7) & opponentBitboard;
        }

        return singlePush | doublePush | attackWest | attackEast;
    }


    public static long enPassant(long pawnsBitboard, long enPassantSquareBitboard, boolean isWhite) {
        long epAttackWest;
        long epAttackEast;

        if (isWhite) {
            // 1. If the piece is not in the A file and there is an en passant target up and left 1 square, it can move there
            // 2. If the piece is not in the H file and there is an en passant target up and right 1 square, it can move there
            epAttackWest = ((pawnsBitboard & ~0x0101010101010101L) << 7) & enPassantSquareBitboard;
            epAttackEast = ((pawnsBitboard & ~0x8080808080808080L) << 9) & enPassantSquareBitboard;

            return epAttackWest | epAttackEast;
        }

        else {
            // 1. If the piece is not in the H file and there is an en passant target up and left 1 square, it can move there
            // 2. If the piece is not in the A file and there is an en passant target up and right 1 square, it can move there
            epAttackWest = ((pawnsBitboard & ~0x0101010101010101L) >> 9) & enPassantSquareBitboard;
            epAttackEast = ((pawnsBitboard & ~0x8080808080808080L) >> 7) & enPassantSquareBitboard;
        }

        return epAttackWest | epAttackEast;
    }
}
