package com.chessbot.BoardUtils;

import com.chessbot.Objects.Board;
import com.chessbot.Objects.Pieces.Bishop;
import com.chessbot.Objects.Pieces.Rook;
import com.chessbot.ViewManager;

public class PseudoLegalMoves {
    // Pseudo legal moves are legal moves that don't check if their king is in check after they are played, the program
    // first generates pseudo legal moves to instantly get legal moves after. All the piece methods are static because
    // they concern all the knights, for example, not a single knight object
    public static void generateOpponentPseudoLegalMoves(Board board, int opponentColour) {
        // Resets each turn
        long allPseudoLegalMovesBitboard = 0;

        // Pawn pseudo legal moves
        //allPseudoLegalMovesBitboard |= Pawn.pseudoLegalMoves[opponentColour].generate(bitboards[opponentColour][1], otherBitboards[2], otherBitboards[opponentColour ^ 1]);

        // Knight pseudo legal moves
        //allPseudoLegalMovesBitboard |= Knight.pseudoLegalMoves(getBitboard(opponentColour, 2), otherBitboards[opponentColour]);

        // Rook pseudo legal moves, from the rooks bitboard, get each rook's square, find what pieces are blocking its
        // path, from that and the precomputed magic numbers and best bits, get the magicIndex, enter those keys in the
        // rook lookup table and get the rook's pseudo legal moves, also remove all friendly piece captures before adding
        // the moves to the all pseudo legal moves bitboard. Additionally calculating rook pseudo legal moves for the queen
        long[] rooksQueensBitboards = {board.getBitboard(opponentColour, 4), board.getBitboard(opponentColour, 5)};
        for (int i = 1; i < 2; i += 1) {
            while (rooksQueensBitboards[i] != 0L) {
                int rookSquare = Long.numberOfTrailingZeros(rooksQueensBitboards[i]);
                long blockingPatternBitboard = board.getOtherBitboard(2) & Rook.attacks(rookSquare);
                int magicIndex = (int) ((blockingPatternBitboard * board.getRookMagicNumbers(rookSquare)) >>> 64 - board.getRookBestBits(rookSquare));

                long rookPseudoLegalMovesBitboard = board.getRookMoves(board.getRookOffsets(rookSquare), magicIndex);
                rookPseudoLegalMovesBitboard &= ~board.getOtherBitboard(opponentColour);
                allPseudoLegalMovesBitboard |= rookPseudoLegalMovesBitboard;

                rooksQueensBitboards[i] ^= (1L << rookSquare);
            }
        }

        // Bishop/Queen pseudo legal moves, same process as the rooks
        long[] bishopsQueensBitboards = {board.getBitboard(opponentColour, 3), board.getBitboard(opponentColour, 5)};
        for (int i = 1; i < 2; i += 1) {
            while (bishopsQueensBitboards[i] != 0L) {
                int bishopSquare = Long.numberOfTrailingZeros(bishopsQueensBitboards[i]);
                long blockingPatternBitboard = board.getOtherBitboard(2) & Bishop.attacks(bishopSquare);
                int magicIndex = (int) ((blockingPatternBitboard * board.getBishopMagicNumbers(bishopSquare)) >>> 64 - board.getBishopBestBits(bishopSquare));

                long rookPseudoLegalMovesBitboard = board.getBishopMoves(board.getBishopOffsets(bishopSquare), magicIndex);
                rookPseudoLegalMovesBitboard &= ~board.getOtherBitboard(opponentColour);
                allPseudoLegalMovesBitboard |= rookPseudoLegalMovesBitboard;

                bishopsQueensBitboards[i] ^= (1L << bishopSquare);
            }
        }

        board.setAllPseudoLegalMovesBitboard(allPseudoLegalMovesBitboard);

        ViewManager.instance.bitboardVisualization(allPseudoLegalMovesBitboard);
    }
}
