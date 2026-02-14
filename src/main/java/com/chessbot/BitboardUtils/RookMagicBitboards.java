package com.chessbot.BitboardUtils;

import com.chessbot.Objects.Pieces.Rook;

public class RookMagicBitboards extends MagicBitboards {
    @Override
    public long getAttacks(int square) {
        return Rook.attacks(square);
    }

    @Override
    public long getPseudoLegalMoves(int square, long blockingPatternsBitboard) {
        return Rook.pseudoLegalMoves(square, blockingPatternsBitboard);
    }
}
