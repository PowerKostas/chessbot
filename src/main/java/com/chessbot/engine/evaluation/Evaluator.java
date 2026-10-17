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

    private static final int[] CENTER_MANHATTAN_DISTANCE = {
            6, 5, 4, 3, 3, 4, 5, 6,
            5, 4, 3, 2, 2, 3, 4, 5,
            4, 3, 2, 1, 1, 2, 3, 4,
            3, 2, 1, 0, 0, 1, 2, 3,
            3, 2, 1, 0, 0, 1, 2, 3,
            4, 3, 2, 1, 1, 2, 3, 4,
            5, 4, 3, 2, 2, 3, 4, 5,
            6, 5, 4, 3, 3, 4, 5, 6
    };

    private Evaluator() {}


    // If a checkmate sequence requires more moves than the target search depth, the engine will likely shuffle pieces endlessly.
    // This function gives bonuses for bringing the winning king closer to the losing king and for pushing the losing king
    // to the edges. PSTs already penalize the king being in the edges, but just that is not enough
    private static int calculateMopUpScore(Board board, int losingColor) {
        int whiteKingSquare = Long.numberOfTrailingZeros(board.getBitboard(Piece.WHITE, Piece.KING));
        int blackKingSquare = Long.numberOfTrailingZeros(board.getBitboard(Piece.BLACK, Piece.KING));
        int fileDistance = Math.abs((whiteKingSquare & 7) - (blackKingSquare & 7));
        int rankDistance = Math.abs((whiteKingSquare >>> 3) - (blackKingSquare >>> 3));
        int distanceBetweenKings = fileDistance + rankDistance;
        int losingKingSquare = (losingColor == Piece.WHITE) ? whiteKingSquare : blackKingSquare;

        // The smaller the distance between the kings, the bigger the bonus. The maximum file + rank distance is 14
        int kingProximityBonus = (14 - distanceBetweenKings) * KING_PROXIMITY_MULTIPLIER;

        // Bonus for forcing the losing king to the edges
        int losingKingEdgeBonus = CENTER_MANHATTAN_DISTANCE[losingKingSquare] * LOSING_KING_EDGE_MULTIPLIER;

        return kingProximityBonus + losingKingEdgeBonus;
    }


    // Coordinates every job of the evaluation function
    public static int evaluate(Board board, boolean experimentalVersion) {
        int maxPhase = Material.getMaxPhase();

        // Tapered evaluation is used to make a smooth transition between the phases of the game. In the starting position
        // phase = MAX_PHASE, but that value can technically be exceeded with promotions, so phase is capped to MAX_PHASE
        // in order for the formula to work properly. Phase = MAX_PHASE means it's a pure middlegame, phase = 0 means it's
        // a pure endgame. It gets the current material and Piece-Square Tables scores, first assuming we are in a middlegame
        // and then assuming we are in an endgame. The formula below is used to find the current evaluation, it will be
        // between the pure middlegame and pure endgame evaluations. Opening values are not included because the opening
        // and middlegame are very similar for an engine and because opening books are used
        int cappedPhase = Math.min(board.getPhase(), maxPhase);
        int evaluation = ((board.getCurrentMgTotalScore() * cappedPhase) + (board.getCurrentEgTotalScore() * (maxPhase - cappedPhase))) / maxPhase;
        int tempoBonus = ((getEvalParam(MG_TEMPO_BONUS_INDEX) * cappedPhase) + (getEvalParam(EG_TEMPO_BONUS_INDEX) * (maxPhase - cappedPhase))) / maxPhase;

        // Only apply the mop-up score if one player is up at least a rook and the other player doesn't have any pawns
        int egRookValue = getEvalParam(EG_PIECE_VALUES_START_INDEX + Piece.ROOK);
        if ((evaluation > egRookValue) && (board.getBitboard(Piece.BLACK, Piece.PAWN) == 0L)) {
            evaluation += calculateMopUpScore(board, Piece.BLACK);
        }

        else if ((evaluation < -egRookValue) && (board.getBitboard(Piece.WHITE, Piece.PAWN) == 0L)) {
            evaluation -= calculateMopUpScore(board, Piece.WHITE);
        }

        // Returns the appropriately signed evaluation for the current player, both white and black get a positive/negative
        // evaluation when winning/lossing. Have to change the perspective because the scores are calculated as positive/negative
        // when white/black is winning
        return (board.getTurn() == Piece.WHITE) ? evaluation + tempoBonus : -evaluation + tempoBonus;
    }
}
