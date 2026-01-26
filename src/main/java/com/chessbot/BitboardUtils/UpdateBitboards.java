package com.chessbot.BitboardUtils;

import com.chessbot.Objects.Board;

// Goes here after every move
// For the bitboards, colour/piece type, colour, all, adds an 1 to the 64 bit long variable, the 1 is in the
// position of the piece, eg, piece in the 3rd row and 4th column = bit 27
public class UpdateBitboards {
    public static void start(Board board, int pieceColour, int pieceType, int oldSquareIndex, int newSquareIndex) {
        long removeMask = 1L << oldSquareIndex;
        long addMask = 1L << newSquareIndex;

        long bitboard = board.getBitboard(pieceColour, pieceType);
        bitboard &= ~removeMask;
        bitboard |= addMask;
        board.setBitboard(pieceColour, pieceType, bitboard);

        long colourBitboard = board.getOtherBitboard(pieceColour);
        colourBitboard &= ~removeMask;
        colourBitboard |= addMask;
        board.setOtherBitboard(pieceColour, colourBitboard);

        long allBitboard = board.getOtherBitboard(2);
        allBitboard &= ~removeMask;
        allBitboard |= addMask;
        board.setOtherBitboard(2, allBitboard);

        // Resets the en passant bitboard after each move, and if a pawn moved 2 squares up, the square 1 up is an
        // en passant target, don't worry about the bitwise operations, they work
        long tempEnPassantSquareBitboard = 0;
        if (pieceType == 1 && (newSquareIndex ^ oldSquareIndex) == 16) {
            tempEnPassantSquareBitboard = 1L << (newSquareIndex ^ 8);
        }

        board.setEnPassantSquareBitboard(tempEnPassantSquareBitboard);
    }
}
