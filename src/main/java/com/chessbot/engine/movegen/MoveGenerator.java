package com.chessbot.engine.movegen;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Pieces.King;

public final class MoveGenerator {
    private MoveGenerator() {}


    // Filters pseudo legal moves into legal moves
    public static void generate(Board board) {
        // Resets legal moves each turn
        board.clearLegalMoves();

        int friendlyColor = board.getTurn();
        int enemyColor = friendlyColor ^ 1;
        long friendlyPieces = board.getOtherBitboard(friendlyColor);

        // Gets all the squares that the opponent is attacking
        AttackMap.generate(board, enemyColor);
        long attackMapBitboard = board.getAttackMapBitboard(enemyColor);


        // King pseudo legal moves filtering
        // Gets the squares that the king wants to go
        long friendlyKingBitboard = board.getBitboard(friendlyColor, 5);
        int kingSquare = Long.numberOfTrailingZeros(friendlyKingBitboard);
        long kingPseudoLegalMoves = King.pseudoLegalMoves(friendlyKingBitboard, friendlyPieces);

        // Filters out the king moves that place him in check
        long kingLegalMoves = kingPseudoLegalMoves & ~attackMapBitboard;

        // Places each king legal move to the array in the Board class
        while (kingLegalMoves != 0L) {
            int endingSquare = Long.numberOfTrailingZeros(kingLegalMoves);
            int move = Move.createMove(kingSquare, endingSquare, 0);
            board.addLegalMove(move);

            kingLegalMoves ^= (1L << endingSquare);
        }
    }
}
