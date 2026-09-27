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

    // Evaluation function parameters, tuned with Texel's Tuning
    public static final int[] MG_PIECE_VALUES = {82, 337, 365, 477, 1025, 0};
    public static final int[] EG_PIECE_VALUES = {94, 281, 297, 512,  936, 0};
    public static final int[] MAX_PIECE_VALUES = {94, 337, 365, 512, 1025, 0};
    public static final int[] PHASE_WEIGHTS = {0, 11, 11, 21, 42, 0};

    private Constants() {}
}
