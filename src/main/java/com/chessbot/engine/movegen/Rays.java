package com.chessbot.engine.movegen;

public class Rays {
    // A bitboard of an orthogonal (rook attacks) or diagonal (bishop attacks) line passing through 2 squares for every single
    // combination of 2 squares in the board. For example, if the indexes c3 and f6 are given, the line includes the squares a1, b2,
    // c3, d4, e5, f6, g7 and h8. Being given c3 and f6 is the same as being given f6 and c3, but it's better to not compress the
    // table in order to avoid an if check in the engine
    public static final long[] LINE = new long[4096]; // 64x64

    // Works in the same way as LINE, but it's a bitboard of the squares strictly between the 2 given squares
    public static final long[] BETWEEN = new long[4096];


    // Precomputes the LINE and BETWEEN constants
    static {
        for (int square1 = 0; square1 < 64; square1 += 1) {
            for (int square2 = 0; square2 < 64; square2++) {
                // If it's a combination of the same square, bitboard value remains 0
                if (square1 == square2) {
                    continue;
                }

                int rankSquare1 = square1 / 8;
                int fileSquare1 = square1 % 8;
                int rankSquare2 = square2 / 8;
                int fileSquare2 = square2 % 8;

                int deltaRank = rankSquare2 - rankSquare1;
                int deltaFile = fileSquare2 - fileSquare1;

                // Only orthogonal or diagonal combinations of squares are calculated. 2 squares are orthogonal if they are
                // in the same rank or file and diagonal if the horizontal distance between them equals the vertical distance
                boolean isOrthogonal = (deltaRank == 0 || deltaFile == 0);
                boolean isDiagonal = (Math.abs(deltaRank) == Math.abs(deltaFile));
                if (isOrthogonal || isDiagonal) {
                    // Signum returns 1 for positive numbers, 0 for 0 and -1 for negative numbers. This way we can navigate
                    // the board one square at the time, either orthogonally or diagonally. Orthogonally when either stepRank
                    // or stepFile are zero and diagonally when both hold non-zero values
                    int stepRank = Integer.signum(deltaRank);
                    int stepFile = Integer.signum(deltaFile);

                    // Calculates the LINE bitboard for this combination, starts from square1 and extends positively using stepRank
                    // and stepFile
                    long lineBitboard = 0L;
                    int currentRank = rankSquare1;
                    int currentFile = fileSquare1;
                    while (currentRank >= 0 && currentRank < 8 && currentFile >= 0 && currentFile < 8) {
                        lineBitboard |= (1L << (currentRank * 8 + currentFile));
                        currentRank += stepRank;
                        currentFile += stepFile;
                    }

                    // Same as above, starts from the square before square1, because square1 has already been included, and extends
                    // negatively
                    currentRank = rankSquare1 - stepRank;
                    currentFile = fileSquare1 - stepFile;
                    while (currentRank >= 0 && currentRank < 8 && currentFile >= 0 && currentFile < 8) {
                        lineBitboard |= (1L << (currentRank * 8 + currentFile));
                        currentRank -= stepRank;
                        currentFile -= stepFile;
                    }

                    // Adds the lineBitboard to the array for this combination. The LINE array is flattened for efficiency, an index
                    // is used to access it, (square1 << 6) | square2 is the same as (square1 * 64) + square2
                    int index = (square1 << 6) | square2;
                    LINE[index] = lineBitboard;

                    // Calculates the BETWEEN bitboard for this combination, starts from the square after square1 and extends to
                    // the square before square2
                    long betweenBitboard = 0L;
                    currentRank = rankSquare1 + stepRank;
                    currentFile = fileSquare1 + stepFile;
                    while (currentRank != rankSquare2 || currentFile != fileSquare2) {
                        betweenBitboard |= (1L << (currentRank * 8 + currentFile));
                        currentRank += stepRank;
                        currentFile += stepFile;
                    }

                    BETWEEN[index] = betweenBitboard;
                }
            }
        }
    }
}
