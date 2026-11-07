package com.chessbot.engine.utils;

import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Piece;

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


    public static String moveToString(int move) {
        int startingSquare = Move.getStartingSquare(move);
        int endingSquare = Move.getEndingSquare(move);

        StringBuilder moveString = new StringBuilder();
        moveString.append(indexToString(startingSquare));
        moveString.append(indexToString(endingSquare));

        // If the move is a promotion, append the promoted piece letter
        int flag = Move.getFlag(move);
        if (flag >= Move.FLAG_KNIGHT_PROMOTION) {
            int promotedPiece = (flag & 3) + 1;

            if (promotedPiece == Piece.KNIGHT) {
                moveString.append("n");
            }

            else if (promotedPiece == Piece.BISHOP) {
                moveString.append("b");
            }

            else if (promotedPiece == Piece.ROOK) {
                moveString.append("r");
            }

            else {
                moveString.append("q");
            }
        }

        return moveString.toString();
    }
}
