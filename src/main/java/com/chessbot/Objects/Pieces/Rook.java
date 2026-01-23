package com.chessbot.Objects.Pieces;

import com.chessbot.Objects.Piece;

public class Rook extends Piece {
    public Rook(int colour, boolean reverse) { super(colour, 4, reverse); }


    public static long rookAttacks(int square) {
        // rankMasks: 0xFF = Rank 1, 0xFF00 = Rank 2 ..., fileMasks: 0x0101010101010101 = File A ...
        long[] rankMasks = {0xFFL, 0xFF00L, 0xFF0000L, 0xFF000000L, 0xFF00000000L, 0xFF0000000000L, 0xFF000000000000L, 0xFF00000000000000L};
        long[] fileMasks = {0x0101010101010101L, 0x0202020202020202L, 0x0404040404040404L, 0x0808080808080808L, 0x1010101010101010L, 0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L};

        // Gets the rank and file of the square that the rook is in and returns all the squares in that rank/file except
        // of the square that the rook is in
        int rank = square / 8;
        int file = square % 8;

        return (rankMasks[rank] | fileMasks[file]) ^ square;
    }
}
