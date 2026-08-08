package com.chessbot.engine.magic;

import com.chessbot.engine.core.Pieces.Bishop;

public class BishopMagicBitboards extends MagicBitboards {
    @Override
    public long getAllAttacks(int square) {
        return Bishop.allAttacks(square);
    }

    @Override
    public long getSlowAttacks(int square, long blockingPatternsBitboard) {
        return Bishop.slowAttacks(square, blockingPatternsBitboard);
    }
}
