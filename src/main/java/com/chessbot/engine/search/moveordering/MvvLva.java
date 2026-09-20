package com.chessbot.engine.search.moveordering;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;

// MVV-LVA (Most Valuable Victim - Least Valuable Aggressor) gives a score boost to captures, the score positively scales
// with favorable piece value disparities. Captures should be searched before normal moves and a pawn capturing a queen
// should be searched before a queen capturing a pawn
final class MvvLva {
    private MvvLva() {}


    // The function is given a move, if it's a capture, it's scored based on this formula: ((victim + 1) << 3) - attacker.
    // The + 1 part is needed because pawns have a value of 0. The << 3 part is needed to avoid score collisions, without
    // it for example, a pawn capturing a rook would get a higher score than a bishop capturing a queen. The score ranges
    // from a value of 4 (queen capturing a pawn) to a value of 40 (pawn capturing a queen). The formula results are used
    // in other parts of the code, so try not to tweak the formula
    public static int scoreMove(Board board, int move) {
        int score = 0;
        int flag = Move.getFlag(move);

        // En passant captures need to be handled separately, just give a static score
        if (flag == Move.FLAG_EN_PASSANT_CAPTURE) {
            score = 8;
        }

        else if (flag == Move.FLAG_CAPTURE || flag >= Move.FLAG_KNIGHT_PROMOTION_CAPTURE) {
            int startingSquare = Move.getStartingSquare(move);
            int endingSquare = Move.getEndingSquare(move);

            int attacker = board.getPieceTypeAtSquare(startingSquare);
            int victim = board.getPieceTypeAtSquare(endingSquare);
            score = ((victim + 1) << 3) - attacker;
        }

        return score;
    }
}
