package com.chessbot.engine.utils;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.movegen.MoveList;

public final class ResultDetector {
    private ResultDetector() {}


    public static boolean isCheckmate(MoveList moveList, boolean inCheck) {
        return moveList.count == 0 && inCheck;
    }

    public static boolean isStalemate(MoveList moveList, boolean inCheck) { return moveList.count == 0 && !inCheck; }

    public static boolean isFiftyMoveRule(Board board) {
        return board.getHalfMoveClock() >= 100;
    }

    public static boolean isInsufficientMaterial(Board board) {
        // If there are any pawns, rooks, or queens on the board, checkmate is always possible
        long majorPieces = board.getBitboard(Piece.WHITE, Piece.PAWN) | board.getBitboard(Piece.BLACK, Piece.PAWN) |
                           board.getBitboard(Piece.WHITE, Piece.ROOK) | board.getBitboard(Piece.BLACK, Piece.ROOK) |
                           board.getBitboard(Piece.WHITE, Piece.QUEEN) | board.getBitboard(Piece.BLACK, Piece.QUEEN);

        if (majorPieces != 0L) {
            return false;
        }

        // If there is 0 or 1 minor piece on the board, it's a draw
        long knights = board.getBitboard(Piece.WHITE, Piece.KNIGHT) | board.getBitboard(Piece.BLACK, Piece.KNIGHT);
        long bishops = board.getBitboard(Piece.WHITE, Piece.BISHOP) | board.getBitboard(Piece.BLACK, Piece.BISHOP);
        long minorPieces = knights | bishops;

        if (Long.bitCount(minorPieces) <= 1) return true;

        // There is an insufficient material edge case where if there are only 1 or more bishops for each player, if they are
        // all on same colored squares, it's a draw. Checks if there are no knights (so there are only bishops) and if all bishops
        // are on dark or light squares (so all bishops are on same colored squares)
        long lightSquaredBishops = bishops & 0x55AA55AA55AA55AAL; // 0x55AA55AA55AA55AAL = All light squares
        return knights == 0L && (lightSquaredBishops == 0L || lightSquaredBishops == bishops);
    }

    public static boolean isThreefoldRepetition(Board board) {
        int halfMoveClock = board.getHalfMoveClock();
        int zobristHistoryIndex = board.getZobristHistoryIndex();
        long[] zobristHistory = board.getZobristHistory();
        long currentZobristKey = board.getCurrentZobristKey();

        // The current position counts as 1 repetition
        int repetitions = 1;

        // A repeated position can only start happening after 4 half moves. If a move that reset the half move clock
        // happened, positions past that point cant be repetitions. That's why it searches backwards up to the half move clock
        // limit for repeated positions. Step by 2 because a repetition can only happen on the same player's turn
        for (int i = zobristHistoryIndex - 4; i >= zobristHistoryIndex - halfMoveClock; i -= 2) {
            if (zobristHistory[i] == currentZobristKey) {
                repetitions += 1;

                if (repetitions == 3) {
                    return true;
                }
            }
        }

        return false;
    }
}
