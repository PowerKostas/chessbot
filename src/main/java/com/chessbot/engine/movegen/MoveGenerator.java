package com.chessbot.engine.movegen;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Pieces.*;

public final class MoveGenerator {
    private MoveGenerator() {}


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
        long friendlyKingBitboard = board.getBitboard(friendlyColor, 5);
        int kingSquare = Long.numberOfTrailingZeros(friendlyKingBitboard);
        long kingPseudoLegalMoves = King.pseudoLegalMoves(friendlyKingBitboard, friendlyPieces);

        // Filters out the king moves that put the king in check
        long safeKingMoves = kingPseudoLegalMoves & ~attackMapBitboard;

        // Places each king safe move to the legal moves array
        while (safeKingMoves != 0L) {
            int endingSquare = Long.numberOfTrailingZeros(safeKingMoves);
            int move = Move.createMove(kingSquare, endingSquare, 0);
            board.addLegalMove(move);

            safeKingMoves ^= (1L << endingSquare);
        }


        // Check detection
        long checkers = Checkers.calculateSquares(board, friendlyColor);
        int checkCount = Long.bitCount(checkers);

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
        long pawnsBitboard = board.getBitboard(friendlyColor, 0);
        while (pawnsBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(pawnsBitboard);
            long pawnBitboard = 1L << startingSquare;

            long pawnLegalMoves = Pawn.pseudoLegalMoves(pawnBitboard, allPieces, enemyPieces, friendlyColor == 0) & checkMask;
            while (pawnLegalMoves != 0L) {
                int endingSquare = Long.numberOfTrailingZeros(pawnLegalMoves);
                board.addLegalMove(Move.createMove(startingSquare, endingSquare, 0));

                pawnLegalMoves ^= 1L << endingSquare;
            }

            pawnsBitboard ^= pawnBitboard;
        }

        long knightsBitboard = board.getBitboard(friendlyColor, 1);
        while (knightsBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(knightsBitboard);
            long knightBitboard = 1L << startingSquare;

            long knightLegalMoves = Knight.pseudoLegalMoves(knightBitboard, friendlyPieces) & checkMask;
            while (knightLegalMoves != 0L) {
                int endingSquare = Long.numberOfTrailingZeros(knightLegalMoves);
                board.addLegalMove(Move.createMove(startingSquare, endingSquare, 0));

                knightLegalMoves ^= 1L << endingSquare;
            }

            knightsBitboard ^= knightBitboard;
        }

        long bishopsBitboard = board.getBitboard(friendlyColor, 2);
        while (bishopsBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(bishopsBitboard);
            long bishopLegalMoves = Bishop.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces) & checkMask;

            while (bishopLegalMoves != 0L) {
                int endingSquare = Long.numberOfTrailingZeros(bishopLegalMoves);
                board.addLegalMove(Move.createMove(startingSquare, endingSquare, 0));

                bishopLegalMoves ^= 1L << endingSquare;
            }

            bishopsBitboard ^= 1L << startingSquare;
        }

        long rooksBitboard = board.getBitboard(friendlyColor, 3);
        while (rooksBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(rooksBitboard);
            long rookLegalMoves = Rook.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces) & checkMask;

            while (rookLegalMoves != 0L) {
                int endingSquare = Long.numberOfTrailingZeros(rookLegalMoves);
                board.addLegalMove(Move.createMove(startingSquare, endingSquare, 0));

                rookLegalMoves ^= (1L << endingSquare);
            }

            rooksBitboard ^= (1L << startingSquare);
        }

        long queensBitboard = board.getBitboard(friendlyColor, 4);
        while (queensBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(queensBitboard);
            long queenLegalMoves = (Rook.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces) |
                                   Bishop.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces)) &
                                   checkMask;

            while (queenLegalMoves != 0L) {
                int endingSquare = Long.numberOfTrailingZeros(queenLegalMoves);
                board.addLegalMove(Move.createMove(startingSquare, endingSquare, 0));

                queenLegalMoves ^= (1L << endingSquare);
            }

            queensBitboard ^= (1L << startingSquare);
        }
    }
}
