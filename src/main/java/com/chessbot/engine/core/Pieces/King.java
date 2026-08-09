package com.chessbot.engine.core.Pieces;

public final class King {
    private King() {}


    // Similar logic to Knight.attacks because both pieces can just attack their allowed squares, if a friendly piece isn't there
    public static long attacks(long pieceBitboard) {
        long upleft, left, downleft;
        upleft = left = downleft = pieceBitboard & ~0x0101010101010101L;

        long upright, right, downright;
        upright = right = downright = pieceBitboard & ~0x8080808080808080L;

        return right << 1 | upleft << 7 | pieceBitboard << 8 | upright << 9 |
               left >>> 1 | downright >>> 7 | pieceBitboard >>> 8 | downleft >>> 9;
    }


    // Same logic as Knight.pseudoLegalMoves
    public static long pseudoLegalMoves(long pieceBitboard, long friendlyPiecesBitboard) {
        return attacks(pieceBitboard) & ~friendlyPiecesBitboard;
    }
}
