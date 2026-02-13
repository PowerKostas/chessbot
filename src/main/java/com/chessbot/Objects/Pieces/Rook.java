package com.chessbot.Objects.Pieces;

import com.chessbot.Objects.Piece;

public class Rook extends Piece {
    public Rook(int colour, boolean reverse) { super(colour, 4, reverse); }

    // rankMasks: 0xFF = Rank 1, 0xFF00 = Rank 2 ..., fileMasks: 0x0101010101010101 = File A ...
    private static final long[] rankMasks = {0xFFL, 0xFF00L, 0xFF0000L, 0xFF000000L, 0xFF00000000L, 0xFF0000000000L, 0xFF000000000000L, 0xFF00000000000000L};
    private static final long[] fileMasks = {0x0101010101010101L, 0x0202020202020202L, 0x0404040404040404L, 0x0808080808080808L, 0x1010101010101010L, 0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L};


    public static long attacks(int square) {
        // Treats the board as empty, gets the rank and file of the square that the rook is in and returns all the squares
        // in that rank/file except of the edge square, because we use these attacks for blocking patterns and a piece on
        // the edge of the board doesn't block anything (we treat all pieces as enemy pieces, for now), also doesn't return
        // the square that the rook is in
        int rank = square / 8;
        int file = square % 8;

        long rankAttacks = rankMasks[rank] & 0x7E7E7E7E7E7E7E7EL;
        long fileAttacks = fileMasks[file] & 0x00FFFFFFFFFFFF00L;

        return (rankAttacks | fileAttacks) & ~(1L << square);
    }


    public static long pseudoLegalMoves(int square, long blockingPatternBitboard) {
        long attacksBitboard = 0L;
        int[] directions = {8, -8, -1, 1}; // Up 1 square, down 1 square, left 1 square, right 1 square

        for (int dir : directions) {
            int tempSquare = square;

            while (true) {
                if (dir == -1 && (tempSquare % 8 == 0)) { // If trying to go left and on the A file
                    break;
                }

                else if (dir == 1 && (tempSquare % 8 == 7)) { // If trying to go right and on the H file
                    break;
                }

                else if (dir == -8 && tempSquare <= 7) { // If trying to go down and on the 1st rank
                    break;
                }

                else if (dir == 8 && tempSquare >= 56) { // If trying to go up and on the 8th rank
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
