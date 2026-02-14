package com.chessbot.Objects.Pieces;

import com.chessbot.Objects.Piece;

public class Bishop extends Piece {
    public Bishop(int colour, boolean reverse) {
        super(colour, 3, reverse);
    }

    // diagonalMasks: From left to right, antiDiagonalMasks: From right to left
    private static final long[] diagonalMasks = {0x80L, 0x8040L, 0x804020L, 0x80402010L, 0x8040201008L, 0x804020100804L, 0x80402010080402L, 0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L, 0x1008040201000000L, 0x804020100000000L, 0x402010000000000L, 0x201000000000000L, 0x100000000000000L};
    private static final long[] antiDiagonalMasks = {0x1L, 0x102L, 0x10204L, 0x1020408L, 0x102040810L, 0x10204081020L, 0x1020408102040L, 0x102040810204080L, 0x204081020408000L, 0x408102040800000L, 0x810204080000000L, 0x1020408000000000L, 0x2040800000000000L, 0x4080000000000000L, 0x8000000000000000L};


    // Same logic as Rook.attacks
    public static long attacks(int square) {
        int rank = square / 8;
        int file = square % 8;

        long diagonal = diagonalMasks[rank - file + 7] & 0x007E7E7E7E7E7E00L;
        long antiDiagonal = antiDiagonalMasks[rank + file] & 0x007E7E7E7E7E7E00L;

        return (diagonal | antiDiagonal) & ~(1L << square);
    }


    // Same logic as Rook.pseudoLegalMoves
    public static long pseudoLegalMoves(int square, long blockingPatternBitboard) {
        long attacksBitboard = 0L;
        int[] directions = {7, 9, -7, -9}; // Up and left 1 square, up and right 1 square, down and right 1 square, down and left 1 square

        for (int dir : directions) {
            int tempSquare = square;

            while (true) {
                if ((dir == 7 || dir == - 9) && (tempSquare % 8 == 0)) { // If trying to go left and on the A file
                    break;
                }

                else if ((dir == 9 || dir == -7) && (tempSquare % 8 == 7)) { // If trying to go right and on the H file
                    break;
                }

                else if ((dir == -7 || dir == -9) && tempSquare <= 7) { // If trying to go down and on the 1st rank
                    break;
                }

                else if ((dir == 7 || dir == 9) && tempSquare >= 56) { // If trying to go up and on the 8th rank
                    break;
                }

                // If it didn't go in on any safety check, move 1 square in that direction
                tempSquare += dir;
                attacksBitboard |= 1L << tempSquare;

                // If that move has a blocking piece on it (friendly or enemy, will deal with friendly pieces later), we
                // accept the move and then stop
                if (((1L << tempSquare) & blockingPatternBitboard) != 0) {
                    break;
                }
            }
        }

        return attacksBitboard;
    }
}
