package com.chessbot.engine.core;

// Creates the int objects that occupy Board.legalMoves. The first 6 bits (2 ^ 6 = 64) of the int object are reserved for the
// starting square, the next 6 for the ending square and the next 4 for the flags (capture, en passant, castling and promotions)
public final class Move {
    private Move() {}


    private static final int startingSquareMask = 0b111111;
    private static final int endingSquareMask = 0b111111 << 6;

    // Packs the starting square, ending square and flags into an integer
    public static int createMove(int startingSquare, int endingSquare, int flags) {
        return startingSquare | (endingSquare << 6) | (flags << 12);
    }

    // Unpacks the integer to get the starting square
    public static int getStartingSquare(int move) {
        return move & startingSquareMask;
    }

    // Unpacks the integer to get the ending square
    public static int getEndingSquare(int move) {
        return (move & endingSquareMask) >>> 6;
    }
}
