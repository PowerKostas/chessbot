package com.chessbot.BoardUtils;

import com.chessbot.Objects.Board;

// Goes here at the start of the game (FenReader)
// For the bitboards, colour/piece type, colour, all, adds an 1 to the 64 bit long variable, the 1 is in the
// position of the piece, eg, piece in the 3rd row and 4th column = bit 27
public class SetBitboards {
    public static void start(Board board, int pieceColour, int pieceType, int newSquareIndex) {
        long addMask = 1L << newSquareIndex;

        long bitboard = board.getBitboard(pieceColour, pieceType);
        bitboard |= addMask;
        board.setBitboard(pieceColour, pieceType, bitboard);

        long colourBitboard = board.getOtherBitboard(pieceColour);
        colourBitboard |= addMask;
        board.setOtherBitboard(pieceColour, colourBitboard);

        long allBitboard = board.getOtherBitboard(2);
        allBitboard |= addMask;
        board.setOtherBitboard(2, allBitboard);
    }
}
