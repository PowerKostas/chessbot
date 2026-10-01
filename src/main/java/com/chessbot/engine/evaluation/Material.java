package com.chessbot.engine.evaluation;

import com.chessbot.engine.core.Constants;

import static com.chessbot.engine.core.Constants.*;

public final class Material {
    /**
     * The values that the pawn, knight, bishop, rook, queen and king have in the middlegame and endgame
     * @see Constants#MG_PIECE_VALUES_START_INDEX
     * @see Constants#EG_PIECE_VALUES_START_INDEX
     * @see Constants#MAX_PIECE_VALUES

     * Weights that indicate how much a pawn, knight, bishop, rook, queen or king affects endgame detection. Pawns get a
     * value of 0 because a pawns and king position is always an endgame, no matter the number of pawns
     * @see Constants#PHASE_WEIGHTS_START_INDEX
     */

    private Material() {}


    // Getters are used instead of making the fields public because array values are mutable despite the array being final
    public static int getMgPieceValue(int pieceType) { return EVAL_PARAMS[MG_PIECE_VALUES_START_INDEX + pieceType]; }

    public static int getEgPieceValue(int pieceType) { return EVAL_PARAMS[EG_PIECE_VALUES_START_INDEX + pieceType]; }

    public static int getMaxPieceValue(int pieceType) { return MAX_PIECE_VALUES[pieceType]; }

    public static int getPhaseWeight(int pieceType) { return EVAL_PARAMS[PHASE_WEIGHTS_START_INDEX + pieceType]; }
}
