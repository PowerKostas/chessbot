package com.chessbot.engine.utils;

import java.util.SplittableRandom;

// Zobrist hashing is used to compress an entire chess position in a 64-bit hash key. The position can't be uncompressed, that's
// why the key is used only for checking if 2 positions are identical. The key is created by XORing random longs that represent
// the needed data. The data is every piece currently on the board, the turn, the castling rights and the en passant file if
// needed. This way a unique random key can be generated for any position. For 64-bit keys, 2 keys might collide once every 4
// billion positions, but that is acceptable. This class precomputes all the random longs for each needed piece of data
public final class Zobrist {
    private Zobrist() {}

    // 12 piece types * 64 squares
    public static final long[][] PIECES = new long[12][64];

    // A random long is only added if it's black's turn, otherwise 0 will be added
    public static final long TURN;

    // 16 possible castling right combinations exist between both players
    public static final long[] CASTLING_RIGHTS = new long[16];

    // 8 possible files for an en passant square. The specific square is not needed because a unique position can be identified
    // with just the file in combination with the turn
    public static final long[] EN_PASSANT_FILE = new long[8];


    static {
        // SplittableRandom is used for better quality random numbers. A fixed seed allows the engine to always generate the same
        // Zobrist keys, useful for debugging
        SplittableRandom random = new SplittableRandom(42);

        for (int pieceType = 0; pieceType < 12; pieceType++) {
            for (int square = 0; square < 64; square++) {
                PIECES[pieceType][square] = random.nextLong();
            }
        }

        TURN = random.nextLong();

        for (int i = 0; i < 16; i++) {
            CASTLING_RIGHTS[i] = random.nextLong();
        }

        for (int i = 0; i < 8; i++) {
            EN_PASSANT_FILE[i] = random.nextLong();
        }
    }
}
