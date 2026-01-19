package com.chessbot.PieceUtils;

public interface PawnPseudoLegalMoves {
    long generate(long pawns, long allPieces, long opponentPieces, long enPassantSquareBitboard);
}
