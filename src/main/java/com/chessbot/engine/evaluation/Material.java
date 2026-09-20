package com.chessbot.engine.evaluation;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;

public final class Material {
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 320;
    public static final int BISHOP_VALUE = 330;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;
    public static final int KING_VALUE = 20000;
    private static final int[] PIECE_VALUES = {100, 320, 330, 500, 900, 20000};

    private Material() {}


    // Uses a getter for array elements because they are mutable even if the array is final
    public static int getPieceValue(int pieceType) { return PIECE_VALUES[pieceType]; }


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
