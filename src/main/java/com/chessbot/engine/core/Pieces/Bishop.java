package com.chessbot.engine.core.Pieces;

import com.chessbot.engine.magic.MagicConstants;

// This class uses similar logic to Rook
public final class Bishop {
    // DIAGONAL_MASKS: From left to right, ANTI_DIAGONAL_MASKS: From right to left
    private static final long[] DIAGONAL_MASKS = {0x80L, 0x8040L, 0x804020L, 0x80402010L, 0x8040201008L, 0x804020100804L,
                                                  0x80402010080402L, 0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L,
                                                  0x1008040201000000L, 0x804020100000000L, 0x402010000000000L, 0x201000000000000L,
                                                  0x100000000000000L};

    private static final long[] ANTI_DIAGONAL_MASKS = {0x1L, 0x102L, 0x10204L, 0x1020408L, 0x102040810L, 0x10204081020L,
                                                       0x1020408102040L, 0x102040810204080L, 0x204081020408000L, 0x408102040800000L,
                                                       0x810204080000000L, 0x1020408000000000L, 0x2040800000000000L, 0x4080000000000000L,
                                                       0x8000000000000000L};

    private Bishop() {}


    public static long allAttacks(int square) {
        int rank = square / 8;
        int file = square % 8;

        long diagonal = DIAGONAL_MASKS[rank - file + 7] & 0x007E7E7E7E7E7E00L;
        long antiDiagonal = ANTI_DIAGONAL_MASKS[rank + file] & 0x007E7E7E7E7E7E00L;

        return (diagonal | antiDiagonal) & ~(1L << square);
    }


    public static long slowAttacks(int square, long blockingPatternBitboard) {
        long attacksBitboard = 0L;

        // Up and left 1 square, up and right 1 square, down and right 1 square, down and left 1 square
        int[] directions = {7, 9, -7, -9};

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

                tempSquare += dir;
                attacksBitboard |= 1L << tempSquare;

                if (((1L << tempSquare) & blockingPatternBitboard) != 0) {
                    break;
                }
            }
        }

        return attacksBitboard;
    }


    public static long attacks(int square, long allPiecesBitboard) {
        long blockingPatternBitboard = allPiecesBitboard & allAttacks(square);
        int magicIndex = (int) ((blockingPatternBitboard * MagicConstants.BISHOP_MAGIC_NUMBERS[square]) >>> 64 - MagicConstants.BISHOP_BEST_BITS[square]);
        return MagicConstants.getBishopMoves(square, magicIndex);
    }


    public static long pseudoLegalMoves(int square, long allPiecesBitboard, long friendlyPiecesBitboard) {
        return attacks(square, allPiecesBitboard) & ~friendlyPiecesBitboard;
    }
}
