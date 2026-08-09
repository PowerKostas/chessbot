package com.chessbot.engine.movegen;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Pieces.Bishop;
import com.chessbot.engine.core.Pieces.Knight;
import com.chessbot.engine.core.Pieces.Pawn;
import com.chessbot.engine.core.Pieces.Rook;

public final class Checkers {
    private Checkers() {}


    // Calculates all the squares where the enemy checking pieces are. It starts from the friendly king and finds the squares
    // that a pawn, for example, would attack from there. If an enemy pawn is actually there, it adds it to the checkers
    // variable. King calculations are not included because a king can't put another king in check
    public static long calculateSquares(Board board, int friendlyColor) {
        long checkers = 0L;
        int enemyColor = friendlyColor ^ 1;
        long kingBitboard = board.getBitboard(friendlyColor, 5);
        int kingSquare = Long.numberOfTrailingZeros(kingBitboard);
        long allPiecesBitboard = board.getOtherBitboard(2);

        checkers |= Pawn.attacks(kingBitboard, friendlyColor == 0) & board.getBitboard(enemyColor, 0);
        checkers |= Knight.attacks(kingBitboard) & board.getBitboard(enemyColor, 1);

        // Also checks if a queen is there for bishop/rook attacks
        checkers |= Bishop.attacks(kingSquare, allPiecesBitboard) & (board.getBitboard(enemyColor, 2) | board.getBitboard(enemyColor, 4));
        checkers |= Rook.attacks(kingSquare, allPiecesBitboard) & (board.getBitboard(enemyColor, 3) | board.getBitboard(enemyColor, 4));

        return checkers;
    }


    // Other than moving the king away, rook, bishop and queen checks can be stopped by blocking and capturing, pawn and knight
    // checks can only be stopped by capturing. This function calculates all the squares that stop the check
    public static long calculateEvadeMask(Board board, int friendlyColor, int checkerSquare) {
        long kingBitboard = board.getBitboard(friendlyColor, 5);
        int kingSquare = Long.numberOfTrailingZeros(kingBitboard);
        long allPiecesBitboard = board.getOtherBitboard(2);
        long checkerBitboard = 1L << checkerSquare;

        // The attacker's square is part of the evadeMask because you can always just capture it
        long evadeMask = checkerBitboard;

        // Treats the board as empty and gets all the rook/bishop attacks from the king's square, doesn't use Rook/Bishop.allAttacks
        // because those functions are not considering the edges of the board
        long kingRookVision = Rook.attacks(kingSquare, 0L);
        long kingBishopVision = Bishop.attacks(kingSquare, 0L);

        // If the checker is inside the king rook/bishop vision, add all the squares between the king and the checker in the evadeMask
        if ((kingRookVision & checkerBitboard) != 0) {
            evadeMask |= Rook.attacks(kingSquare, allPiecesBitboard) & Rook.attacks(checkerSquare, allPiecesBitboard);
        }

        else if ((kingBishopVision & checkerBitboard) != 0) {
            evadeMask |= Bishop.attacks(kingSquare, allPiecesBitboard) & Bishop.attacks(checkerSquare, allPiecesBitboard);
        }

        return evadeMask;
    }
}
