package com.chessbot.engine.core;

// Creates the int objects that occupy Board.legalMoves. The first 6 bits (2 ^ 6 = 64) of the int object are reserved for the
// starting square, the next 6 for the ending square and the next 4 for the moveFlag (quiet, capture, en passant, ...)
public final class Move {
    private static final int STARTING_SQUARE_MASK = 0b111111;
    private static final int ENDING_SQUARE_MASK = 0b111111 << 6;
    private static final int FLAG_MASK = 0b1111 << 12;

    public static final int FLAG_QUIET = 0;
    public static final int FLAG_CAPTURE = 1;
    public static final int FLAG_EN_PASSANT = 2;

    private Move() {}


    // Packs the starting square, ending square and moveFlag into an integer
    public static int createMove(int startingSquare, int endingSquare, int moveFlag) {
        return startingSquare | (endingSquare << 6) | (moveFlag << 12);
    }

    // Unpacks the integer to get the starting square
    public static int getStartingSquare(int move) {
        return move & STARTING_SQUARE_MASK;
    }

    // Unpacks the integer to get the ending square
    public static int getEndingSquare(int move) {
        return (move & ENDING_SQUARE_MASK) >>> 6;
    }

    public static int getFlag(int move) { return (move & FLAG_MASK) >>> 12; }
}
