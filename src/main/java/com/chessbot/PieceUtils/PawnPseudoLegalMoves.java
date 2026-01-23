package com.chessbot.PieceUtils;

public interface PawnPseudoLegalMoves {
    long generate(long pawnsBitboard, long allPiecesBitboard, long opponentPiecesBitboard);
}
