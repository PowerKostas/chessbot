package com.chessbot.engine.magic;

import com.chessbot.engine.core.Pieces.Rook;

public class RookMagicBitboards extends MagicBitboards {
    @Override
    public long getAllAttacks(int square) {
        return Rook.allAttacks(square);
    }

    @Override
    public long getSlowAttacks(int square, long blockingPatternsBitboard) {
        return Rook.slowAttacks(square, blockingPatternsBitboard);
    }
}
