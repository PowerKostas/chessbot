package com.chessbot.engine.core.Pieces;

public final class Knight {
    private Knight() {}


    public static long pseudoLegalMoves(long knightBitboard, long ownPiecesBitboard) {
        // If a knight is on the A file, and it tries to do a 2 up/down and 1 left move, it will go out of bounds. It's
        // similar for an 1 up/down and 2 left move on the B file, a 2 up/down and 1 right move on the H file and an
        // 1 up/down and 2 right move on the G file. 0x0101010101010101L all A file squares are 1, 0x0202020202020202L all
        // B file squares are 1, etc. The first 4 variables keep all the knights that can do the corresponding move. Then
        // create a bitboard with all the remaining legal knight moves, also remove attacks that fall on friendly pieces
        long west1 = knightBitboard & ~0x0101010101010101L;
        long west2 = knightBitboard & ~0x0202020202020202L;
        long east1 = knightBitboard & ~0x8080808080808080L;
        long east2 = knightBitboard & ~0x4040404040404040L;

        long attacks = (west1 << 15) | (west1 >> 17) |
                       (west2 << 6)  | (west2 >> 10) |
                       (east1 << 17) | (east1 >> 15) |
                       (east2 << 10) | (east2 >> 6);

        return attacks & ~ownPiecesBitboard;
    }
}
