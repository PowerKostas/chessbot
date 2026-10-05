package com.chessbot.engine.evaluation;

import com.chessbot.engine.core.Constants;

import static com.chessbot.engine.core.Constants.*;

public final class PieceSquareTables {
    /**
     * Assigns middlegame and endgame values to specific pieces on specific squares. The first square on the middlegame
     * and endgame PST piece tables is a8
     *
     * @see Constants#MG_PST_START_INDEX
     * @see Constants#EG_PST_START_INDEX
     */

    // Precomputed flattened arrays of middlegame or endgame PST + material scores. The first 384 elements (6 piece types
    // 64 squares) are for white, the next 384 are for black. Because the middlegame and endgame PST piece tables start
    // from a8 and the engine starts from a1, white has to reverse the board vertically when initializing these tables
    private static final int[] MG_SCORE_TABLE = new int[768];
    private static final int[] EG_SCORE_TABLE = new int[768];

    // Executes on startup to initialize MG_SCORE_TABLE and EG_SCORE_TABLE
    static {
        for (int i = 0; i < EVAL_PARAMS_COUNT; i += 1) {
            updateScoreTables(i);
        }
    }

    private PieceSquareTables() {}


    public static int getMgScore(int pieceColor, int pieceType, int square) {
        return MG_SCORE_TABLE[pieceColor * 384 + pieceType * 64 + square];
    }

    public static int getEgScore(int pieceColor, int pieceType, int square) {
        return EG_SCORE_TABLE[pieceColor * 384 + pieceType * 64 + square];
    }


    // Works in a similar way to Material.updateMaterialValues
    public static void updateScoreTables(int index) {
        // When a middlegame material piece value changes, update MG_SCORE_TABLE
        if (index >= MG_PIECE_VALUES_START_INDEX && index < EG_PIECE_VALUES_START_INDEX) {
            int pieceType = index - MG_PIECE_VALUES_START_INDEX;

            for (int square = 0; square < 64; square += 1) {
                int mgPstIndex = MG_PST_START_INDEX + (pieceType * 64) + square;
                int combinedScore = getEvalParam(MG_PIECE_VALUES_START_INDEX + pieceType) + getEvalParam(mgPstIndex);

                // White and black MG table mapping
                MG_SCORE_TABLE[pieceType * 64 + (square ^ 56)] = combinedScore;
                MG_SCORE_TABLE[384 + pieceType * 64 + square]  = combinedScore;
            }
        }

        // When an endgame material piece value changes, update EG_SCORE_TABLE
        else if (index >= EG_PIECE_VALUES_START_INDEX && index < PHASE_WEIGHTS_START_INDEX) {
            int pieceType = index - EG_PIECE_VALUES_START_INDEX;

            for (int square = 0; square < 64; square += 1) {
                int egPstIndex = EG_PST_START_INDEX + (pieceType * 64) + square;
                int combinedValue = getEvalParam(EG_PIECE_VALUES_START_INDEX + pieceType) + getEvalParam(egPstIndex);

                // White and black EG table mapping
                EG_SCORE_TABLE[pieceType * 64 + (square ^ 56)] = combinedValue;
                EG_SCORE_TABLE[384 + pieceType * 64 + square]  = combinedValue;
            }
        }

        // When a middlegame PST value changes, update MG_SCORE_TABLE
        else if (index >= MG_PST_START_INDEX && index < EG_PST_START_INDEX) {
            int indexOffset = index - MG_PST_START_INDEX;
            int pieceType = indexOffset >> 6;
            int square = indexOffset & 63;
            int combinedValue = getEvalParam(MG_PIECE_VALUES_START_INDEX + pieceType) + getEvalParam(index);

            MG_SCORE_TABLE[pieceType * 64 + (square ^ 56)] = combinedValue;
            MG_SCORE_TABLE[384 + pieceType * 64 + square]  = combinedValue;
        }

        // When an endgame PST value changes, update EG_SCORE_TABLE
        else if (index >= EG_PST_START_INDEX && index < EVAL_PARAMS_COUNT) {
            int indexOffset = index - EG_PST_START_INDEX;
            int pieceType = indexOffset >> 6;
            int square = indexOffset & 63;
            int combinedValue = getEvalParam(EG_PIECE_VALUES_START_INDEX + pieceType) + getEvalParam(index);

            EG_SCORE_TABLE[pieceType * 64 + (square ^ 56)] = combinedValue;
            EG_SCORE_TABLE[384 + pieceType * 64 + square]  = combinedValue;
        }
    }
}
