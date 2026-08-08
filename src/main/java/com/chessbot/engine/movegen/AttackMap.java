package com.chessbot.engine.movegen;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Pieces.*;

public final class AttackMap {
    private AttackMap() {}


    // An attack map calculates all the squares that are being attacked by any piece of the opponent. The color parameter
    // indicates which player's attack map will be generated
    public static void generate(Board board, int color) {
        // Filters out the friendly king in the calculations. This is done in order to avoid the king being able to move backwards
        // in the same direction as a checking slider when filtering the attack map to legal moves
        long allPiecesBitboard = board.getOtherBitboard(2) & ~board.getBitboard(color ^ 1, 5);

        // Resets each turn
        long attackMapBitboard = 0L;

        // Pawn attack map
        attackMapBitboard |= Pawn.attacks(board.getBitboard(color, 0), color == 0);

        // Knight attack map
        attackMapBitboard |= Knight.attacks(board.getBitboard(color, 1));

        // Rook attack map, it also calculates a rook attack map for the queen
        long[] rooksQueensBitboards = {board.getBitboard(color, 3), board.getBitboard(color, 4)};
        for (int i = 0; i < 2; i += 1) {
            while (rooksQueensBitboards[i] != 0L) {
                int rookSquare = Long.numberOfTrailingZeros(rooksQueensBitboards[i]);
                attackMapBitboard |= Rook.attacks(rookSquare, allPiecesBitboard);
                rooksQueensBitboards[i] ^= (1L << rookSquare);
            }
        }

        // Bishop/Queen attack map, same process as the rooks
        long[] bishopsQueensBitboards = {board.getBitboard(color, 2), board.getBitboard(color, 4)};
        for (int i = 0; i < 2; i += 1) {
            while (bishopsQueensBitboards[i] != 0L) {
                int bishopSquare = Long.numberOfTrailingZeros(bishopsQueensBitboards[i]);
                attackMapBitboard |= Bishop.attacks(bishopSquare, allPiecesBitboard);
                bishopsQueensBitboards[i] ^= (1L << bishopSquare);
            }
        }

        // King attack map
        attackMapBitboard |= King.attacks(board.getBitboard(color, 5));

        board.setAttackMapBitboard(color, attackMapBitboard);
    }
}
