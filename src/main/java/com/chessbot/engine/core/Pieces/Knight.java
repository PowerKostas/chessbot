package com.chessbot.engine.core.Pieces;

public final class Knight {
    private Knight() {}


    // If a knight is on the A file, and it tries to do a 2 up/down and 1 left move, it will go out of bounds. It's similar for
    // an 1 up/down and 2 left move on the A and B files, a 2 up/down and 1 right move on the H file and an 1 up/down and
    // 2 right move on the G and H files. 0x0101010101010101L all A file squares are 1, 0x0303030303030303L all A and B files
    // squares are 1 ... The first 4 variables keep all the knights that can do the corresponding move. Then it creates a bitboard
    // with all the remaining legal knight moves. Doesn't check for possibilities of upwards and downwards out of bounds because
    // they will just be off the 64-bit bitboard
    public static long attacks(long pieceBitboard) {
        long west1 = pieceBitboard & ~0x0101010101010101L;
        long west2 = pieceBitboard & ~0x0303030303030303L;
        long east1 = pieceBitboard & ~0x8080808080808080L;
        long east2 = pieceBitboard & ~0xC0C0C0C0C0C0C0C0L;

        return west1 << 15 | west1 >>> 17 |
               west2 << 6  | west2 >>> 10 |
               east1 << 17 | east1 >>> 15 |
               east2 << 10 | east2 >>> 6;
    }


    // Only difference of knight attacks and knight pseudo legal moves are that the pseudo legal moves can't fall on friendly pieces
    public static long pseudoLegalMoves(long pieceBitboard, long friendlyPiecesBitboard) {
        return attacks(pieceBitboard) & ~friendlyPiecesBitboard;
    }
}
