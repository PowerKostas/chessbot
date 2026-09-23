package com.chessbot.engine.evaluation;

public final class Material {
    // The values that the pawn, knight, bishop, rook, queen and king have in the middlegame and endgame
    private static final int[] MG_PIECE_VALUES = {82, 337, 365, 477, 1025, 0};
    private static final int[] EG_PIECE_VALUES = {94, 281, 297, 512,  936, 0};
    private static final int[] MAX_PIECE_VALUES = {94, 337, 365, 512, 1025, 0};

    // Standard values that indicate how much a pawn, knight, bishop, rook, queen or king affects endgame detection. Pawns
    // get a value of 0 because a pawns and king position is always an endgame, no matter the number of pawns. The original
    // values were {0, 1, 1, 2, 4, 0} but they got scaled up in order for their starting material based sum to equal exactly
    // 256 which is a power of 2. This enables faster calculations in the Evaluator class
    private static final int[] PHASE_WEIGHTS = {0, 11, 11, 21, 42, 0};

    private Material() {}


    // Getters are used instead of making the fields public because array values are mutable despite the array being final
    public static int getMgPieceValue(int pieceType) { return MG_PIECE_VALUES[pieceType]; }

    public static int getEgPieceValue(int pieceType) { return EG_PIECE_VALUES[pieceType]; }

    public static int getMaxPieceValue(int pieceType) { return MAX_PIECE_VALUES[pieceType]; }

    public static int getPhaseWeight(int pieceType) { return PHASE_WEIGHTS[pieceType]; }
}
