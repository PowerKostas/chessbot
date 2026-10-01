package com.chessbot.engine.evaluation;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Constants;
import com.chessbot.engine.core.Piece;

import static com.chessbot.engine.core.Constants.*;

// All evaluations are measured in centipawns
public final class Evaluator {
    /**
     * A tempo bonus portrays that it's usually advantageous to have the move and being able to do something, except in
     * zugzwang positions. That bonus is useful mainly in the opening and middlegame, but it can be counterproductive in
     * the endgame
     * @see Constants#MG_TEMPO_BONUS_INDEX
     * @see Constants#EG_TEMPO_BONUS_INDEX
     */

    private Evaluator() {}


    // Coordinates every job of the evaluation function
    public static int evaluate(Board board, boolean experimentalVersion) {
        // Tapered evaluation is used to make a smooth transition between the phases of the game. In the starting position
        // phase = MAX_PHASE, but that value can technically be exceeded with promotions, so phase is capped to MAX_PHASE
        // in order for the formula to work properly. Phase = MAX_PHASE means it's a pure middlegame, phase = 0 means it's
        // a pure endgame. It gets the current material and Piece-Square Tables scores, first assuming we are in a middlegame
        // and then assuming we are in an endgame. The formula below is used to find the current evaluation, it will be
        // between the pure middlegame and pure endgame evaluations. Opening values are not included because the opening
        // and middlegame are very similar for an engine and because opening books are used
        int cappedPhase = Math.min(board.getPhase(), MAX_PHASE);
        int evaluation = ((board.getCurrentMgScore() * cappedPhase) + (board.getCurrentEgScore() * (MAX_PHASE - cappedPhase))) / MAX_PHASE;
        int tempoBonus = ((EVAL_PARAMS[MG_TEMPO_BONUS_INDEX] * cappedPhase) + (EVAL_PARAMS[EG_TEMPO_BONUS_INDEX] * (MAX_PHASE - cappedPhase))) / MAX_PHASE;

        // Returns the appropriately signed evaluation for the current player, both white and black get a positive/negative
        // evaluation when winning/lossing. Have to change the perspective because the scores are calculated as positive/negative
        // when white/black is winning
        return (board.getTurn() == Piece.WHITE) ? evaluation + tempoBonus : -evaluation + tempoBonus;
    }
}
