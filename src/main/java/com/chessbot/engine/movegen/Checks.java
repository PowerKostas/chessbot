package com.chessbot.engine.movegen;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.core.Pieces.Bishop;
import com.chessbot.engine.core.Pieces.Knight;
import com.chessbot.engine.core.Pieces.Pawn;
import com.chessbot.engine.core.Pieces.Rook;

public final class Checks {
    private Checks() {}


    // Calculates all the squares with enemy checking pieces. It starts from the friendly king and finds the squares that a
    // pawn, for example, would attack from there. If an enemy pawn is actually there, it adds it to the checkers variable. King
    // calculations are not included because a king can't put another king in check
    public static long calculateSquares(Board board, int friendlyColor) {
        long checkers = 0L;
        int enemyColor = friendlyColor ^ 1;
        long kingBitboard = board.getBitboard(friendlyColor, Piece.KING);
        int kingSquare = Long.numberOfTrailingZeros(kingBitboard);
        long allPiecesBitboard = board.getOtherBitboard(2);

        checkers |= Pawn.attacks(friendlyColor, kingBitboard) & board.getBitboard(enemyColor, Piece.PAWN);
        checkers |= Knight.attacks(kingBitboard) & board.getBitboard(enemyColor, Piece.KNIGHT);

        // Also checks if a queen is there for bishop/rook attacks
        checkers |= Bishop.attacks(kingSquare, allPiecesBitboard) & (board.getBitboard(enemyColor, Piece.BISHOP) | board.getBitboard(enemyColor, Piece.QUEEN));
        checkers |= Rook.attacks(kingSquare, allPiecesBitboard) & (board.getBitboard(enemyColor, Piece.ROOK) | board.getBitboard(enemyColor, Piece.QUEEN));

        return checkers;
    }


    // Other than moving the king away, rook, bishop and queen checks can be stopped by blocking and capturing, pawn and knight
    // checks can only be stopped by capturing. This function calculates all the squares that stop the check
    public static long generateEvadeMask(Board board, int friendlyColor, int checkerSquare) {
        long kingBitboard = board.getBitboard(friendlyColor, Piece.KING);
        int kingSquare = Long.numberOfTrailingZeros(kingBitboard);

        // Returns a bitboard of all the squares between the king and the checker. The checker's square is also part of the
        // evadeMask because you can always just capture it
        return (1L << checkerSquare) | Rays.BETWEEN[(kingSquare << 6) | checkerSquare];
    }
}
