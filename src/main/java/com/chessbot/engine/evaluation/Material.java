package com.chessbot.engine.evaluation;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;

public final class Material {
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;

    private Material() {}


    // Counts the total material value of the given player
    public static int count(Board board, int color) {
        int material = 0;

        material += Long.bitCount(board.getBitboard(color, Piece.PAWN)) * PAWN_VALUE;
        material += Long.bitCount(board.getBitboard(color, Piece.KNIGHT)) * KNIGHT_VALUE;
        material += Long.bitCount(board.getBitboard(color, Piece.BISHOP)) * BISHOP_VALUE;
        material += Long.bitCount(board.getBitboard(color, Piece.ROOK)) * ROOK_VALUE;
        material += Long.bitCount(board.getBitboard(color, Piece.QUEEN)) * QUEEN_VALUE;

        return material;
    }
}
