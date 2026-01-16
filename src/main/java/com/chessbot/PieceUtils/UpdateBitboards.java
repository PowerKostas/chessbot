package com.chessbot.PieceUtils;

import com.chessbot.Objects.Board;

// For the bitboards, colour/piece type, colour, all, adds an 1 to the 64 bit long variable, the 1 is in the
// position of the piece, eg, piece in the 3rd row and 4th column = bit 27
public class UpdateBitboards {
    public static void start(Board board, int pieceColour, int pieceType, int oldSquareIndex, int newSquareIndex) {
        // If oldSquareIndex = -1 it means that we just want to add a piece to the board (start of the game), not remove
        // and add (normal move)
        long removeMask = (oldSquareIndex != -1) ? (1L << oldSquareIndex) : 0;
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
    }
}
