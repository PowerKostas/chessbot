package com.chessbot.engine.evaluation;

import com.chessbot.engine.core.Constants;

public final class Material {
    /**
     * The values that the pawn, knight, bishop, rook, queen and king have in the middlegame and endgame
     * @see Constants#MG_PIECE_VALUES
     * @see Constants#EG_PIECE_VALUES
     * @see Constants#MAX_PIECE_VALUES

     * Weights that indicate how much a pawn, knight, bishop, rook, queen or king affects endgame detection. Pawns get a
     * value of 0 because a pawns and king position is always an endgame, no matter the number of pawns. The original values
     * were {0, 1, 1, 2, 4, 0} but they got scaled up in order for their starting material based sum to equal exactly 256
     * (power of 2 that enables faster calculations in the Evaluator class)
     * @see Constants#PHASE_WEIGHTS
    */

    private Material() {}


    // Getters are used instead of making the fields public because array values are mutable despite the array being final
    public static int getMgPieceValue(int pieceType) { return Constants.MG_PIECE_VALUES[pieceType]; }

    public static int getEgPieceValue(int pieceType) { return Constants.EG_PIECE_VALUES[pieceType]; }

    public static int getMaxPieceValue(int pieceType) { return Constants.MAX_PIECE_VALUES[pieceType]; }

    public static int getPhaseWeight(int pieceType) { return Constants.PHASE_WEIGHTS[pieceType]; }
}
