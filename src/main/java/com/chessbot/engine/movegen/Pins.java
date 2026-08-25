package com.chessbot.engine.movegen;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.core.Pieces.Bishop;
import com.chessbot.engine.core.Pieces.Rook;


public final class Pins {
    private Pins() {}


    // Calculates all the squares with pinned pieces
    public static long calculatePinnedPiecesSquares(Board board, int friendlyColor) {
        int enemyColor = friendlyColor ^ 1;
        long kingBitboard = board.getBitboard(friendlyColor, Piece.KING);
        int kingSquare = Long.numberOfTrailingZeros(kingBitboard);
        long allPiecesBitboard = board.getOtherBitboard(2);
        long friendlyPiecesBitboard = board.getOtherBitboard(friendlyColor);

        // Resets each turn
        long pinnedPiecesBitboard = 0L;

        // Finds enemy sliding pieces
        long enemyRooksQueens = board.getBitboard(enemyColor, Piece.ROOK) | board.getBitboard(enemyColor, Piece.QUEEN);
        long enemyBishopsQueens = board.getBitboard(enemyColor, Piece.BISHOP) | board.getBitboard(enemyColor, Piece.QUEEN);

        // Finds all the pieces pinned by enemy rooks and queens (just the rook attacks)
        while (enemyRooksQueens != 0L) {
            int sliderSquare = Long.numberOfTrailingZeros(enemyRooksQueens);

            // If a rook or queen is found in a rook attack ray from the king square
            if ((Rook.attacks(kingSquare, 0L) & (1L << sliderSquare)) != 0) {
                // Gets a bitboard of all the squares between the king and the checker, more information about the BETWEEN constant
                // in the Rays class
                long rayBitboard = Rays.BETWEEN[(kingSquare << 6) | sliderSquare];

                // If exactly one piece is in the ray, and it's a friendly piece, it's pinned. Add the pinned piece to the bitboard
                long blockers = rayBitboard & allPiecesBitboard;
                if (Long.bitCount(blockers) == 1) {
                    if ((blockers & friendlyPiecesBitboard) != 0) {
                        pinnedPiecesBitboard |= blockers;
                    }
                }
            }

            enemyRooksQueens ^= (1L << sliderSquare);
        }

        // Finds all the pieces pinned by enemy bishops and queens (just the bishop attacks), same process as the rooks and queens
        while (enemyBishopsQueens != 0L) {
            int sliderSquare = Long.numberOfTrailingZeros(enemyBishopsQueens);

            if ((Bishop.attacks(kingSquare, 0L) & (1L << sliderSquare)) != 0) {
                long ray = Rays.BETWEEN[(kingSquare << 6) | sliderSquare];
                long blockers = ray & allPiecesBitboard;

                if (Long.bitCount(blockers) == 1) {
                    if ((blockers & friendlyPiecesBitboard) != 0) {
                        pinnedPiecesBitboard |= blockers;
                    }
                }
            }

            enemyBishopsQueens ^= (1L << sliderSquare);
        }

        return pinnedPiecesBitboard;
    }
}
