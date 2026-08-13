package com.chessbot.engine.movegen;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.core.Pieces.*;

public final class MoveGenerator {
    private MoveGenerator() {}


    // Places each piece legal move to the legal moves array
    private static void packMoves(Board board, long pieceLegalMoves, int startingSquare, long enemyPieces) {
        while (pieceLegalMoves != 0L) {
            int endingSquare = Long.numberOfTrailingZeros(pieceLegalMoves);

            // Checks if it's a capture or a normal move
            int moveFlag = ((1L << endingSquare) & enemyPieces) != 0 ? Move.FLAG_CAPTURE : Move.FLAG_QUIET;

            board.addLegalMove(Move.createMove(startingSquare, endingSquare, moveFlag));
            pieceLegalMoves ^= (1L << endingSquare);
        }
    }


    // Filters pseudo legal moves into legal moves
    public static void generate(Board board) {
        // Resets legal moves each turn
        board.clearLegalMoves();

        int friendlyColor = board.getTurn();
        int enemyColor = friendlyColor ^ 1;
        long friendlyPieces = board.getOtherBitboard(friendlyColor);
        long enemyPieces = board.getOtherBitboard(enemyColor);
        long allPieces = board.getOtherBitboard(2);

        // Gets all the squares that the opponent is attacking
        AttackMap.generate(board, enemyColor);
        long attackMapBitboard = board.getAttackMapBitboard(enemyColor);


        // King pseudo legal moves filtering
        // Gets the squares that the king wants to go
        long friendlyKingBitboard = board.getBitboard(friendlyColor, Piece.KING);
        int kingSquare = Long.numberOfTrailingZeros(friendlyKingBitboard);
        long kingPseudoLegalMoves = King.pseudoLegalMoves(friendlyKingBitboard, friendlyPieces);

        // Filters out the king moves that put the king in check
        long safeKingMoves = kingPseudoLegalMoves & ~attackMapBitboard;

        // Places each king safe move to the legal moves array
        packMoves(board, safeKingMoves, kingSquare, enemyPieces);


        // Check detection
        long checkers = Checkers.calculateSquares(board, friendlyColor);
        int checkCount = Long.bitCount(checkers);

        // Used in other parts of the code
        board.setInCheck(checkCount > 0);

        // Default value, if there are no checks, safe king moves and all the rest of the pseudo legal moves are allowed
        long checkMask = 0xFFFFFFFFFFFFFFFFL;

        // If there is only 1 check, the legal moves are the safe king moves and all the moves that evade the check, more info
        // on Checkers.calculateEvadeMask
        if (checkCount == 1) {
            int checkerSquare = Long.numberOfTrailingZeros(checkers);
            checkMask = Checkers.calculateEvadeMask(board, friendlyColor, checkerSquare);
        }

        // If there are 2 or more checks, only the king safe moves are legal
        else if (checkCount > 1) {
            return;
        }


        // Rest of the pieces pseudo legal moves filtering
        long pawnsBitboard = board.getBitboard(friendlyColor, Piece.PAWN);
        long enPassantBitboard = board.getEnPassantSquareBitboard();
        boolean isWhite = (friendlyColor == 0);
        while (pawnsBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(pawnsBitboard);
            long pawnBitboard = 1L << startingSquare;
            long pawnLegalMoves = Pawn.pseudoLegalMoves(friendlyColor, pawnBitboard, allPieces, enemyPieces) & checkMask;

            packMoves(board, pawnLegalMoves, startingSquare, enemyPieces);

            // Handles en passant separately because it's a special move
            if (enPassantBitboard != 0L) {
                long enPassantLegalMoves = Pawn.enPassant(pawnBitboard, enPassantBitboard, isWhite) & checkMask;
                if (enPassantLegalMoves != 0L) {
                    int endingSquare = Long.numberOfTrailingZeros(enPassantLegalMoves);
                    board.addLegalMove(Move.createMove(startingSquare, endingSquare, Move.FLAG_EN_PASSANT));
                }
            }

            pawnsBitboard ^= pawnBitboard;
        }

        long knightsBitboard = board.getBitboard(friendlyColor, Piece.KNIGHT);
        while (knightsBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(knightsBitboard);
            long knightBitboard = 1L << startingSquare;
            long knightLegalMoves = Knight.pseudoLegalMoves(knightBitboard, friendlyPieces) & checkMask;

            packMoves(board, knightLegalMoves, startingSquare, enemyPieces);
            knightsBitboard ^= knightBitboard;
        }

        long bishopsBitboard = board.getBitboard(friendlyColor, Piece.BISHOP);
        while (bishopsBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(bishopsBitboard);
            long bishopLegalMoves = Bishop.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces) & checkMask;

            packMoves(board, bishopLegalMoves, startingSquare, enemyPieces);
            bishopsBitboard ^= 1L << startingSquare;
        }

        long rooksBitboard = board.getBitboard(friendlyColor, Piece.ROOK);
        while (rooksBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(rooksBitboard);
            long rookLegalMoves = Rook.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces) & checkMask;

            packMoves(board, rookLegalMoves, startingSquare, enemyPieces);
            rooksBitboard ^= (1L << startingSquare);
        }

        long queensBitboard = board.getBitboard(friendlyColor, Piece.QUEEN);
        while (queensBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(queensBitboard);
            long queenLegalMoves = (Rook.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces) |
                                   Bishop.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces)) &
                                   checkMask;

            packMoves(board, queenLegalMoves, startingSquare, enemyPieces);
            queensBitboard ^= (1L << startingSquare);
        }
    }
}
