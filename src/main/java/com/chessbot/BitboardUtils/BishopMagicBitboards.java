package com.chessbot.BitboardUtils;

import com.chessbot.Objects.Pieces.Bishop;

public class BishopMagicBitboards extends MagicBitboards {
    @Override
    public long getAttacks(int square) {
        return Bishop.attacks(square);
    }

    @Override
    public long getPseudoLegalMoves(int square, long blockingPatternsBitboard) {
        return Bishop.pseudoLegalMoves(square, blockingPatternsBitboard);
    }
}
