package com.chessbot.engine.utils;

public final class CoordinatesConverter {
    private static final String[] SQUARE_STRINGS = new String[64];

    private CoordinatesConverter() {}


    // Precomputes a 64-element array where every index contains the String coordinate of a square (e.g., "e4")
    static {
        for (int i = 0; i < 64; i += 1) {
            char file = (char) ('a' + (i & 7));
            char rank = (char) ('1' + (i >> 3));
            SQUARE_STRINGS[i] = "" + file + rank;
        }
    }


    public static String indexToString(int squareIndex) {
        return SQUARE_STRINGS[squareIndex];
    }


    public static int stringToIndex(String squareString) {
        int file = squareString.charAt(0) - 'a';
        int rank = squareString.charAt(1) - '1';
        return (rank << 3) + file;
    }
}
