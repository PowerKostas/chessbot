package com.chessbot.engine.utils;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;

// Reads a FEN sequence and updates the bitboards
public final class FenParser {
    private FenParser() {}


    private static int getPieceTypeFromLetter(char letter) {
        letter = Character.toLowerCase(letter);

        return switch (letter) {
            case 'p' -> Piece.PAWN;
            case 'n' -> Piece.KNIGHT;
            case 'b' -> Piece.BISHOP;
            case 'r' -> Piece.ROOK;
            case 'q' -> Piece.QUEEN;
            case 'k' -> Piece.KING;
            default -> throw new IllegalArgumentException("Unknown piece: " + letter);
        };
    }


    public static void loadFen(String fen, Board board) {
        int col = 0;
        int row = 7;

        // Example: First part of the starting FEN looks like this rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
        // A lowercase letter represents a black piece, an uppercase letter represents a white piece
        // Numbers indicate the number of empty squares, slashes indicate new rows
        for (char letter : fen.toCharArray()) {
            if (Character.isLetter(letter)) {
                int color = Character.isUpperCase(letter) ? Piece.WHITE : Piece.BLACK;
                int pieceType = getPieceTypeFromLetter(letter);

                // Add piece to the engine board
                board.addPiece(color, pieceType, row * 8 + col);

                col += 1;
            }

            else if (Character.isDigit(letter)) {
                col += letter - '0';
            }

            else if (letter == '/') {
                col = 0;
                row -= 1;
            }
        }
    }
}
