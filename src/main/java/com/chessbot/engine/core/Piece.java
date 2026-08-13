package com.chessbot.engine.core;

// Optional class, but it's clearer in code to reference a String rather than an integer
public final class Piece {
    private Piece() {}


    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int PAWN = 0;
    public static final int KNIGHT = 1;
    public static final int BISHOP = 2;
    public static final int ROOK = 3;
    public static final int QUEEN = 4;
    public static final int KING = 5;
}
