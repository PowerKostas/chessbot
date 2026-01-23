package com.chessbot.PieceUtils;

public interface PawnEnPassant {
    long generate(long pawnsBitboard, long enPassantSquareBitboard);
}
