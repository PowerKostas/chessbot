package com.chessbot.engine.core;

public final class Constants {
    // Stockfish has an absolute maximum search depth of 245 plies, I went with about half of that
    public static final int MAX_SEARCH_DEPTH = 128;

    // The maximum number of plies a single game can last. The longest recorded chess game in history lasted 538 plies,
    // I went with about double of that
    public static final int MAX_GAME_MOVES = 1024;

    // Safe evaluation values that allow the search function operations to work without overflows
    public static final int INFINITY_SCORE = 10000000;
    public static final int CHECKMATE_SCORE = 1000000;

    // Search function parameters, tuned with SPSA
    public static final int[] PROMOTION_SCORES = {-45, -50, -50, 35};
    public static final int SAFETY_MARGIN = 200;
    public static final int LATE_ENDGAME_PHASE_THRESHOLD = 32;

    // Evaluation function parameters, tuned with Texel's Tuning. They are placed in one big array for simplicity in TexelTuner
    public static final int MG_PIECE_VALUES_START_INDEX = 0;
    public static final int EG_PIECE_VALUES_START_INDEX = 6;
    public static final int PHASE_WEIGHTS_START_INDEX = 12;
    public static final int MG_TEMPO_BONUS_INDEX = 18;
    public static final int EG_TEMPO_BONUS_INDEX = 19;

    public static final int EVAL_PARAMS_COUNT = 20;
    public static final int[] UNTUNABLE_EVAL_PARAM_INDICES = {5, 11, 12, 17};
    public static final int[] EVAL_PARAMS = {
        82, 337, 365, 477, 1025, 0,
        94, 281, 297, 512, 936, 0,
        0, 11, 11, 21, 42, 0,
        15,
        5
    };

    // Evaluation function parameters with values that are derived from other evaluation function parameters. These need
    // to get updated every time the original parameters change
    public static final int[] MAX_PIECE_VALUES = {94, 337, 365, 512, 1025, 0};
    public static int MAX_PHASE = 256;

    private Constants() {}


    // In TexelTuner when the original evaluation function parameters change, the derived ones don't. Derived parameters
    // are synced here. MAX_PIECE_VALUES is not included because it's only used in the search function
    public static void syncDerivedEvalParams() {
        MAX_PHASE = 16 * EVAL_PARAMS[PHASE_WEIGHTS_START_INDEX + Piece.PAWN] +
                    4 * EVAL_PARAMS[PHASE_WEIGHTS_START_INDEX + Piece.KNIGHT] +
                    4 * EVAL_PARAMS[PHASE_WEIGHTS_START_INDEX + Piece.BISHOP] +
                    4 * EVAL_PARAMS[PHASE_WEIGHTS_START_INDEX + Piece.ROOK] +
                    2 * EVAL_PARAMS[PHASE_WEIGHTS_START_INDEX + Piece.QUEEN];

    }
}
