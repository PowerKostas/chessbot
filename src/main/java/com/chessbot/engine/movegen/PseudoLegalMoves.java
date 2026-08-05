package com.chessbot.engine.movegen;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Pieces.Bishop;
import com.chessbot.engine.core.Pieces.Rook;
import com.chessbot.engine.magic.MagicConstants;

public final class PseudoLegalMoves {
    private PseudoLegalMoves() {}


    // Pseudo legal moves are legal moves that don't check if their king is in check after they are played. This function is
    // usually used to get all the squares that the opponent is attacking, and based on those to then generate legal moves
    // for the current player. That's why the opponent's color is usually passed in the color parameter
    public static void generate(Board board, int color) {
        long allPieces = board.getOtherBitboard(2);
        long friendlyPieces = board.getOtherBitboard(color);
        long enemyPieces = board.getOtherBitboard(color ^ 1);

        // Resets each turn
        long allPseudoLegalMovesBitboard = 0;

        // Pawn pseudo legal moves
        //allPseudoLegalMovesBitboard |= Pawn.pseudoLegalMoves(board.getBitboard(color, 0), allPieces, enemyPieces, color == 0);

        // Knight pseudo legal moves
        //allPseudoLegalMovesBitboard |= Knight.pseudoLegalMoves(board.getBitboard(color, 1), friendlyPieces);

        // Rook pseudo legal moves. From the rooks bitboard get each rook's square, find what pieces are blocking its
        // path, from that and the precomputed magic numbers and best bits get the magicIndex. Enter those keys in the
        // rook lookup table and get the rook's pseudo legal moves, also remove all friendly piece captures before adding
        // the moves to the all pseudo legal moves bitboard. It also calculates rook pseudo legal moves for the queen
        long[] rooksQueensBitboards = {board.getBitboard(color, 3), board.getBitboard(color, 4)};
        for (int i = 0; i < 2; i += 1) {
            while (rooksQueensBitboards[i] != 0L) {
                int rookSquare = Long.numberOfTrailingZeros(rooksQueensBitboards[i]);
                long blockingPatternBitboard = allPieces & Rook.attacks(rookSquare);
                int magicIndex = (int) ((blockingPatternBitboard * MagicConstants.rookMagicNumbers[rookSquare]) >>> 64 - MagicConstants.rookBestBits[rookSquare]);

                long rookPseudoLegalMovesBitboard = MagicConstants.getRookMoves(rookSquare, magicIndex);
                rookPseudoLegalMovesBitboard &= ~friendlyPieces;
                allPseudoLegalMovesBitboard |= rookPseudoLegalMovesBitboard;

                rooksQueensBitboards[i] ^= (1L << rookSquare);
            }
        }

        // Bishop/Queen pseudo legal moves, same process as the rooks
        long[] bishopsQueensBitboards = {board.getBitboard(color, 2), board.getBitboard(color, 4)};
        for (int i = 0; i < 2; i += 1) {
            while (bishopsQueensBitboards[i] != 0L) {
                int bishopSquare = Long.numberOfTrailingZeros(bishopsQueensBitboards[i]);
                long blockingPatternBitboard = allPieces & Bishop.attacks(bishopSquare);
                int magicIndex = (int) ((blockingPatternBitboard * MagicConstants.bishopMagicNumbers[bishopSquare]) >>> 64 - MagicConstants.bishopBestBits[bishopSquare]);

                long bishopPseudoLegalMovesBitboard = MagicConstants.getBishopMoves(bishopSquare, magicIndex);
                bishopPseudoLegalMovesBitboard &= ~friendlyPieces;
                allPseudoLegalMovesBitboard |= bishopPseudoLegalMovesBitboard;

                bishopsQueensBitboards[i] ^= (1L << bishopSquare);
            }
        }

        board.setAllPseudoLegalMovesBitboard(allPseudoLegalMovesBitboard);
    }
}
