package com.chessbot.engine.core;

// Creates an int object that holds the data that can't be found again after a move has been made. It's used in the search algorithm
// in order to unmake moves. Bits 0-2 = Captured piece type (pawn, bishop, knight, rook, queen, king, none). Bits 3-9 = En passant
// square index, holds values up to 127, 0-63 are the squares of a board and the 64th bit is for when there is no en passant
// square. Bits 10-13 = Castling rights, more information in Board. Bits 14-20 = Half move clock, the maximum number of half
// moves allowed is 100, this holds values up to 127
public final class Undo {
    private static final int CAPTURED_PIECE_TYPE_MASK = 0b111;
    private static final int EN_PASSANT_SQUARE_INDEX_MASK = 0b1111111 << 3;
    private static final int CASTLING_RIGHTS_MASK = 0b1111 << 10;
    private static final int HALF_MOVE_CLOCK_MASK = 0b1111111 << 14;

    public static final int NONE_PIECE_TYPE = 6;
    public static final int NONE_EN_PASSANT_SQUARE = 64;

    private Undo() {}


    public static int createUndo(int capturedPieceType, long enPassantSquareBitboard, int castlingRights, int halfMoveClock) {
        int enPassantSquareIndex = (enPassantSquareBitboard == 0L) ? NONE_EN_PASSANT_SQUARE : Long.numberOfTrailingZeros(enPassantSquareBitboard);
        return capturedPieceType | (enPassantSquareIndex << 3) | (castlingRights << 10) | (halfMoveClock << 14);
    }


    public static int undoCapturedPieceType(int undo) { return undo & CAPTURED_PIECE_TYPE_MASK; }

    public static long undoEnPassantSquareBitboard(int undo) {
        int enPassantSquareIndex = (undo & EN_PASSANT_SQUARE_INDEX_MASK) >>> 3;
        return (enPassantSquareIndex == NONE_EN_PASSANT_SQUARE) ? 0L : (1L << enPassantSquareIndex);
    }

    public static int undoCastlingRights(int undo) { return (undo & CASTLING_RIGHTS_MASK) >>> 10; }

    public static int undoHalfMoveClock(int undo) { return (undo & HALF_MOVE_CLOCK_MASK) >>> 14; }
}
