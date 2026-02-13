package com.chessbot.BoardUtils;

import com.chessbot.Objects.Board;
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
        // the moves to the all pseudo legal moves bitboard
        long rooksBitboard = board.getBitboard(opponentColour, 4);
        while (rooksBitboard != 0L) {
            int rookSquare = Long.numberOfTrailingZeros(rooksBitboard);
            long blockingPatternBitboard = board.getOtherBitboard(2) & Rook.attacks(rookSquare);
            int magicIndex = (int) ((blockingPatternBitboard * board.getRookMagicNumbers(rookSquare)) >>> 64 - board.getRookBestBits(rookSquare));

            long rookPseudoLegalMovesBitboard = board.getRookMoves(board.getRookOffsets(rookSquare), magicIndex);
            rookPseudoLegalMovesBitboard &= ~board.getOtherBitboard(opponentColour);
            allPseudoLegalMovesBitboard |= rookPseudoLegalMovesBitboard;

            rooksBitboard ^= (1L << rookSquare);
        }

        board.setAllPseudoLegalMovesBitboard(allPseudoLegalMovesBitboard);

        ViewManager.instance.bitboardVisualization(allPseudoLegalMovesBitboard);
    }
}
