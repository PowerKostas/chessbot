package com.chessbot.engine.evaluation;

import com.chessbot.engine.core.Constants;
import com.chessbot.engine.core.Piece;

import static com.chessbot.engine.core.Constants.*;

public final class Material {
    /**
     * The values that the pawn, knight, bishop, rook, queen and king have in the middlegame and endgame
     *
     * @see Constants#MG_PIECE_VALUES_START_INDEX
     * @see Constants#EG_PIECE_VALUES_START_INDEX
     *
     * Weights that indicate how much a pawn, knight, bishop, rook, queen or king affects endgame detection. Pawns get a
     * value of 0 because a pawns and king position is always an endgame, no matter the number of pawns
     *
     * @see Constants#PHASE_WEIGHTS_START_INDEX
     */

    // The values are derived from MG_PIECE_VALUES, EG_PIECE_VALUES and PHASE_WEIGHTS
    private static final int[] MAX_PIECE_VALUES = new int[6];
    private static int MAX_PHASE;

    // Executes on startup to initialize MAX_PIECE_VALUES and MAX_PHASE
    static {
        for (int i = 0; i < EVAL_PARAMS_COUNT; i += 1) {
            updateMaterialValues(i);
        }
    }

    private Material() {}


    public static int getMaxPieceValue(int pieceType) { return MAX_PIECE_VALUES[pieceType]; }

    public static int getMaxPhase() { return MAX_PHASE; }


    // This function is also used to sync MAX_PIECE_VALUES and MAX_PHASE other than initializing them. That's because in
    // TexelTuner when some original evaluation function parameters change, the derived score tables don't. Technically
    // MAX_PIECE_VALUES doesn't need to get synced because it's only used in the search function
    public static void updateMaterialValues(int index) {
        // When a middlegame material piece value changes, update MAX_PIECE_VALUES
        if (index >= MG_PIECE_VALUES_START_INDEX && index < EG_PIECE_VALUES_START_INDEX) {
            int pieceType = index - MG_PIECE_VALUES_START_INDEX;
            MAX_PIECE_VALUES[pieceType] = Math.max(getEvalParam(index), getEvalParam(EG_PIECE_VALUES_START_INDEX + pieceType));
        }

        // When an endgame material piece value changes, update MAX_PIECE_VALUES
        else if (index >= EG_PIECE_VALUES_START_INDEX && index < PHASE_WEIGHTS_START_INDEX) {
            int pieceType = index - EG_PIECE_VALUES_START_INDEX;
            MAX_PIECE_VALUES[pieceType] = Math.max(getEvalParam(MG_PIECE_VALUES_START_INDEX + pieceType), getEvalParam(index));
        }

        // When a phase weight changes, update MAX_PHASE
        else if (index >= PHASE_WEIGHTS_START_INDEX && index < MG_TEMPO_BONUS_INDEX) {
            MAX_PHASE = 16 * getEvalParam(PHASE_WEIGHTS_START_INDEX + Piece.PAWN) +
                        4 * getEvalParam(PHASE_WEIGHTS_START_INDEX + Piece.KNIGHT) +
                        4 * getEvalParam(PHASE_WEIGHTS_START_INDEX + Piece.BISHOP) +
                        4 * getEvalParam(PHASE_WEIGHTS_START_INDEX + Piece.ROOK) +
                        2 * getEvalParam(PHASE_WEIGHTS_START_INDEX + Piece.QUEEN);
        }
    }
}
