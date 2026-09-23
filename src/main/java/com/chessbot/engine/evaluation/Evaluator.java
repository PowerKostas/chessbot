package com.chessbot.engine.evaluation;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;

// All evaluations are measured in centipawns
public final class Evaluator {
    private Evaluator() {}


    // Coordinates every job of the evaluation function
    public static int evaluate(Board board, boolean experimentalVersion) {
        // Tapered evaluation is used to make a smooth transition between the phases of the game. In the starting position
        // phase = 256, but that value can technically be exceeded with promotions, so phase is capped to 256 in order for
        // the formula to work properly. Phase = 0 means it's a pure middlegame, phase = 256 means it's a pure endgame.
        // It gets the current material and Piece-Square Tables scores, first assuming we are in a middlegame and then assuming
        // we are in an endgame. The formula below is used to find the current evaluation, it will be between the pure middlegame
        // and pure endgame evaluations. Opening values are not included because the opening and middlegame are very similar
        // for an engine and because opening books are used
        int cappedPhase = Math.min(board.getPhase(), 256);
        int evaluation = ((board.getCurrentMgScore() * cappedPhase) + (board.getCurrentEgScore() * (256 - cappedPhase))) >> 8;

        // Returns the correctly signed evaluation for the current player, both white and black get a positive/negative
        // evaluation when winning/lossing. Have to change the perspective because the scores are calculated as positive/negative
        // when white/black is winning
        return (board.getTurn() == Piece.WHITE) ? evaluation : -evaluation;
    }
}
