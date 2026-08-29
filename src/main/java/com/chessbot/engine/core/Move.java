package com.chessbot.engine.core;

// Creates one of the int objects that occupy Board.legalMoves. The first 6 bits (2 ^ 6 = 64) of the int object are reserved for
// the starting square, the next 6 for the ending square and the next 4 for the moveFlag (quiet, double pawn push, ...). In the
// 4 bits that represent moveFlag, the 4th bit indicates a promotion, the 3rd bit indicates a capture and the 2nd and 1st bits
// are special bits
public final class Move {
    private static final int STARTING_SQUARE_MASK = 0b111111;
    private static final int ENDING_SQUARE_MASK = 0b111111 << 6;
    private static final int FLAG_MASK = 0b1111 << 12;

    public static final int FLAG_QUIET = 0;
    public static final int FLAG_DOUBLE_PAWN_PUSH = 1;
    public static final int FLAG_KING_CASTLE = 2;
    public static final int FLAG_QUEEN_CASTLE = 3;
    public static final int FLAG_CAPTURE = 4;
    public static final int FLAG_EN_PASSANT_CAPTURE = 5;
    public static final int FLAG_KNIGHT_PROMOTION = 8;
    public static final int FLAG_BISHOP_PROMOTION = 9;
    public static final int FLAG_ROOK_PROMOTION = 10;
    public static final int FLAG_QUEEN_PROMOTION = 11;
    public static final int FLAG_KNIGHT_PROMOTION_CAPTURE = 12;
    public static final int FLAG_BISHOP_PROMOTION_CAPTURE = 13;
    public static final int FLAG_ROOK_PROMOTION_CAPTURE = 14;
    public static final int FLAG_QUEEN_PROMOTION_CAPTURE = 15;

    private Move() {}


    public static int createMove(int startingSquare, int endingSquare, int moveFlag) {
        return startingSquare | (endingSquare << 6) | (moveFlag << 12);
    }


    public static int getStartingSquare(int move) { return move & STARTING_SQUARE_MASK; }

    public static int getEndingSquare(int move) {
        return (move & ENDING_SQUARE_MASK) >>> 6;
    }

    public static int getFlag(int move) { return (move & FLAG_MASK) >>> 12; }
}
