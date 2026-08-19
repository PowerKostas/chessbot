package com.chessbot.engine.movegen;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.core.Pieces.*;

public final class MoveGenerator {
    private MoveGenerator() {}


    // Places each legal move of a piece to the legal moves array
    private static void packMoves(Board board, long pieceLegalMoves, int startingSquare, long enemyPieces, int pieceType) {
        while (pieceLegalMoves != 0L) {
            int endingSquare = Long.numberOfTrailingZeros(pieceLegalMoves);
            boolean isCapture = ((1L << endingSquare) & enemyPieces) != 0;

            // If it's a pawn legal move, and it ends on the 1st or 8th rank, it's a promotion. 4 possible legal moves are
            // added in this situation. A knight, bishop, rook or queen promotion or a knight, bishop, rook or queen promotion
            // and capture. Because of how move flag is structured, we can get a piece's flag by adding on to the respective
            // knight flag
            if (pieceType == Piece.PAWN && (endingSquare <= 7 || endingSquare >= 56)) {
                int baseFlag = isCapture ? Move.FLAG_KNIGHT_PROMOTION_CAPTURE : Move.FLAG_KNIGHT_PROMOTION;

                board.addLegalMove(Move.createMove(startingSquare, endingSquare, baseFlag)); // Knight
                board.addLegalMove(Move.createMove(startingSquare, endingSquare, baseFlag + 1)); // Bishop
                board.addLegalMove(Move.createMove(startingSquare, endingSquare, baseFlag + 2)); // Rook
                board.addLegalMove(Move.createMove(startingSquare, endingSquare, baseFlag + 3)); // Queen
            }

            // If it's any other move
            else {
                // Checks if it's a capture or a normal move
                int moveFlag = isCapture ? Move.FLAG_CAPTURE : Move.FLAG_QUIET;

                // Detects if a pawn moved 2 squares up, don't worry about the bitwise operation
                if (pieceType == Piece.PAWN && (endingSquare ^ startingSquare) == 16) {
                    moveFlag = Move.FLAG_DOUBLE_PAWN_PUSH;
                }

                board.addLegalMove(Move.createMove(startingSquare, endingSquare, moveFlag));
            }

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
        packMoves(board, safeKingMoves, kingSquare, enemyPieces, Piece.KING);


        // Check detection
        long checkers = Checkers.calculateSquares(board, friendlyColor);
        int checkCount = Long.bitCount(checkers);

        // Used in other parts of the code
        board.setInCheck(checkCount > 0);

        // Default value, if there are no checks, safe king moves and all the rest of the pseudo legal moves are allowed
        long evadeMask = 0xFFFFFFFFFFFFFFFFL;

        // If there is only 1 check, the legal moves are the safe king moves and all the moves that evade the check, more info
        // on Checkers.calculateEvadeMask
        if (checkCount == 1) {
            int checkerSquare = Long.numberOfTrailingZeros(checkers);
            evadeMask = Checkers.calculateEvadeMask(board, friendlyColor, checkerSquare);
        }

        // If there are 2 or more checks, only the king safe moves are legal
        else if (checkCount > 1) {
            return;
        }


        // Adds special castling moves. Castling is only permitted if the king is not in check. Code is placed here because
        // checks need to be calculated first
        if (!board.getInCheck()) {
            if (friendlyColor == Piece.WHITE) {
                // White kingside castle, checks if that castling right is true and if the f1 and g1 squares are empty and unattacked
                long whiteKingsideEmptyMask = (1L << 5) | (1L << 6);
                if (board.getCastlingRight(Board.WHITE_KINGSIDE)
                        && (allPieces & whiteKingsideEmptyMask) == 0L
                        && (attackMapBitboard & whiteKingsideEmptyMask) == 0L)
                {
                    board.addLegalMove(Move.createMove(4, 6, Move.FLAG_KING_CASTLE));
                }

                // White queenside castle, checks if that right is true, if the b1, c1 and d1 squares are empty and if the c1
                // and d1 squares are unattacked, the b1 square can be attacked and the white king will still be able to
                // queenside castle
                long whiteQueensideEmptyMask = (1L << 1) | (1L << 2) | (1L << 3);
                long whiteQueensideSafeMask = (1L << 2) | (1L << 3);
                if (board.getCastlingRight(Board.WHITE_QUEENSIDE)
                        && (allPieces & whiteQueensideEmptyMask) == 0L
                        && (attackMapBitboard & whiteQueensideSafeMask) == 0L)
                {
                    board.addLegalMove(Move.createMove(4, 2, Move.FLAG_QUEEN_CASTLE));
                }
            }

            // Same logic as above
            else {
                long blackKingsideEmptyMask = (1L << 61) | (1L << 62);
                if (board.getCastlingRight(Board.BLACK_KINGSIDE)
                        && (allPieces & blackKingsideEmptyMask) == 0L
                        && (attackMapBitboard & blackKingsideEmptyMask) == 0L)
                {
                    board.addLegalMove(Move.createMove(60, 62, Move.FLAG_KING_CASTLE));
                }

                long blackQueensideEmptyMask = (1L << 57) | (1L << 58) | (1L << 59);
                long blackQueensideSafeMask = (1L << 58) | (1L << 59);
                if (board.getCastlingRight(Board.BLACK_QUEENSIDE)
                        && (allPieces & blackQueensideEmptyMask) == 0L
                        && (attackMapBitboard & blackQueensideSafeMask) == 0L)
                {
                    board.addLegalMove(Move.createMove(60, 58, Move.FLAG_QUEEN_CASTLE));
                }
            }
        }


        // Rest of the pieces pseudo legal moves filtering
        long pawnsBitboard = board.getBitboard(friendlyColor, Piece.PAWN);
        long enPassantBitboard = board.getEnPassantSquareBitboard();
        boolean isWhite = (friendlyColor == 0);
        while (pawnsBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(pawnsBitboard);
            long pawnBitboard = 1L << startingSquare;
            long pawnLegalMoves = Pawn.pseudoLegalMoves(friendlyColor, pawnBitboard, allPieces, enemyPieces) & evadeMask;

            packMoves(board, pawnLegalMoves, startingSquare, enemyPieces, Piece.PAWN);

            // En passant handling
            if (enPassantBitboard != 0L) {
                // The special case of evading checks by en passant needs to be added separately because the capture happens
                // in a different square than where the pawn is. Finds where the pawn that is about to be captured by en passant
                // actually is, if capturing this pawn evades the check, temporarily add the en passant square to the evade
                // mask
                long epEvadeMask = evadeMask;
                long enPassantPawnBitboard = isWhite ? (enPassantBitboard >>> 8) : (enPassantBitboard << 8);

                if ((evadeMask & enPassantPawnBitboard) != 0L) {
                    epEvadeMask |= enPassantBitboard;
                }

                // Adds en passant moves
                long enPassantLegalMoves = Pawn.enPassant(pawnBitboard, enPassantBitboard, isWhite) & epEvadeMask;
                if (enPassantLegalMoves != 0L) {
                    int endingSquare = Long.numberOfTrailingZeros(enPassantLegalMoves);
                    board.addLegalMove(Move.createMove(startingSquare, endingSquare, Move.FLAG_EN_PASSANT_CAPTURE));
                }
            }

            pawnsBitboard ^= pawnBitboard;
        }

        long knightsBitboard = board.getBitboard(friendlyColor, Piece.KNIGHT);
        while (knightsBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(knightsBitboard);
            long knightBitboard = 1L << startingSquare;
            long knightLegalMoves = Knight.pseudoLegalMoves(knightBitboard, friendlyPieces) & evadeMask;

            packMoves(board, knightLegalMoves, startingSquare, enemyPieces, Piece.KNIGHT);
            knightsBitboard ^= knightBitboard;
        }

        long bishopsBitboard = board.getBitboard(friendlyColor, Piece.BISHOP);
        while (bishopsBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(bishopsBitboard);
            long bishopLegalMoves = Bishop.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces) & evadeMask;

            packMoves(board, bishopLegalMoves, startingSquare, enemyPieces, Piece.BISHOP);
            bishopsBitboard ^= 1L << startingSquare;
        }

        long rooksBitboard = board.getBitboard(friendlyColor, Piece.ROOK);
        while (rooksBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(rooksBitboard);
            long rookLegalMoves = Rook.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces) & evadeMask;

            packMoves(board, rookLegalMoves, startingSquare, enemyPieces, Piece.ROOK);
            rooksBitboard ^= (1L << startingSquare);
        }

        long queensBitboard = board.getBitboard(friendlyColor, Piece.QUEEN);
        while (queensBitboard != 0L) {
            int startingSquare = Long.numberOfTrailingZeros(queensBitboard);
            long queenLegalMoves = (Rook.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces) |
                                   Bishop.pseudoLegalMoves(startingSquare, allPieces, friendlyPieces)) &
                                   evadeMask;

            packMoves(board, queenLegalMoves, startingSquare, enemyPieces, Piece.QUEEN);
            queensBitboard ^= (1L << startingSquare);
        }
    }
}
