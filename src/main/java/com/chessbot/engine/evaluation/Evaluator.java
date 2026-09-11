package com.chessbot.engine.evaluation;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.evaluation.utils.Material;

// All evaluation values are measured in centipawns
public final class Evaluator {
    private Evaluator() {}


    // Coordinates every job of the evaluation function
    public static int evaluate(Board board, boolean experimentalVersion) {
        // Calculates the material value difference of the 2 players
        int whiteEval = Material.count(board, Piece.WHITE);
        int blackEval = Material.count(board, Piece.BLACK);
        int evaluation = whiteEval - blackEval;

        // Returns the correctly signed evaluation score for the current player
        int perspective = (board.getTurn() == Piece.WHITE) ? 1 : -1;
        return evaluation * perspective;
    }
}
