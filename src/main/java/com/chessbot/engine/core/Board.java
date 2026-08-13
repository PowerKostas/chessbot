package com.chessbot.engine.core;

import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.utils.FenParser;

public class Board {
    private int turn = Piece.WHITE;

    // 12 64 bit variables, one for each piece color and piece type, first dimension is the piece color and second dimension
    // is the piece type, each bit indicates a piece on the board, used for board representation
    private final long[][] bitboards = new long[2][6];

    // 0 = White bitboard, 1 = Black bitboard, 2 = All bitboard
    private final long[] otherBitboards = new long [3];

    // 0 = White's attack map for the current turn, = 1 Black's attack map for the current turn
    private final long[] attackMapBitboard = new long[2];

    // 1 for the square that an en passant capture can happen, 0 for everything else
    private long enPassantSquareBitboard = 0L;

    // Holds the legal moves for the current turn. 256 is a safe max limit (the highest number of possible legal moves in any
    // position is 218). The int objects hold info about the legal moves, more information in the Move class
    private final int[] legalMoves = new int[256];

    // Keeps track of how many legal moves are in the array
    private int legalMoveCount = 0;

    // If the friendly king is in check
    private boolean inCheck = false;

    public Board() {}


    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public long getBitboard(int color, int pieceType) {
        return bitboards[color][pieceType];
    }

    public long getOtherBitboard(int index) {
        return otherBitboards[index];
    }

    public long getAttackMapBitboard(int color) {
        return attackMapBitboard[color];
    }

    public void setAttackMapBitboard(int color, long bitboard) { this.attackMapBitboard[color] = bitboard; }

    public long getEnPassantSquareBitboard() {
        return enPassantSquareBitboard;
    }

    public void setEnPassantSquareBitboard(long enPassantSquareBitboard) {this.enPassantSquareBitboard = enPassantSquareBitboard; }

    public int getLegalMove(int index) { return legalMoves[index]; }

    public int getLegalMoveCount() { return legalMoveCount; }

    public boolean getInCheck() { return inCheck; }

    public void setInCheck(boolean inCheck) { this.inCheck = inCheck; }


    // Adds a piece to the board at the start of the game
    public void addPiece(int pieceColor, int pieceType, int squareIndex) {
        long addMask = 1L << squareIndex;

        bitboards[pieceColor][pieceType] |= addMask;
        otherBitboards[pieceColor] |= addMask;
        otherBitboards[2] |= addMask;
    }


    // Moves a piece every turn
    public void movePiece(int startingSquare, int endingSquare, int pieceColor, int pieceType) {
        long removeMask = 1L << startingSquare;
        long addMask = 1L << endingSquare;

        bitboards[pieceColor][pieceType] &= ~removeMask;
        bitboards[pieceColor][pieceType] |= addMask;

        otherBitboards[pieceColor] &= ~removeMask;
        otherBitboards[pieceColor] |= addMask;

        otherBitboards[2] &= ~removeMask;
        otherBitboards[2] |= addMask;

        // Resets the en passant bitboard after each move, and if a pawn moved 2 squares up, the square 1 up is an en passant
        // target, don't worry about the bitwise operations, they work and it's the fastest method
        if (pieceType == Piece.PAWN && (endingSquare ^ startingSquare) == 16) {
            enPassantSquareBitboard = 1L << (endingSquare ^ 8);
        }

        else {
            enPassantSquareBitboard = 0L;
        }
    }


    // Removes a piece whenever a capture happens
    public void removePiece(int pieceColor, int pieceType, int squareIndex) {
        long removeMask = ~(1L << squareIndex);

        bitboards[pieceColor][pieceType] &= removeMask;
        otherBitboards[pieceColor] &= removeMask;
        otherBitboards[2] &= removeMask;
    }


    // Coordinates every job of a move cycle
    public void makeMove(int legalMove) {
        // Gets the necessary info about the move
        int startingSquare = Move.getStartingSquare(legalMove);
        int endingSquare = Move.getEndingSquare(legalMove);
        int flag = Move.getFlag(legalMove);

        int[] pieceInfo = this.getPieceAtSquare(startingSquare);
        int pieceColor = pieceInfo[0];
        int pieceType = pieceInfo[1];
        int enemyColor = pieceColor ^ 1;

        // Removes a piece if necessary, if a normal capture happened, the captured piece is in the ending square
        if (flag == Move.FLAG_CAPTURE) {
            int[] capturedPieceInfo = this.getPieceAtSquare(endingSquare);
            if (capturedPieceInfo != null) {
                this.removePiece(enemyColor, capturedPieceInfo[1], endingSquare);
            }
        }

        // If an en passant capture happened, for white the captured pawn is a rank below the ending square, for black the
        // captured pawn is a rank above the ending square
        else if (flag == Move.FLAG_EN_PASSANT) {
            int capturedPawnSquare = (pieceColor == Piece.WHITE) ? endingSquare - 8 : endingSquare + 8;
            this.removePiece(enemyColor, Piece.PAWN, capturedPawnSquare);
        }

        // Moves the piece
        this.movePiece(startingSquare, endingSquare, pieceColor, pieceType);

        // Flips the turn and generates moves for the next player
        this.turn ^= 1;
        MoveGenerator.generate(this);
    }


    // Coordinates every job at the start of the game
    public void loadPosition(String fen) {
        // Loads pieces onto the board and generates moves for the next player
        FenParser.loadFen(fen, this);
        MoveGenerator.generate(this);
    }


    // Gets the piece info (color and piece type) at a specific square
    public int[] getPieceAtSquare(int squareIndex) {
        long squareMask = 1L << squareIndex;

        for (int color = 0; color < 2; color += 1) {
            for (int pieceType = 0; pieceType < 6; pieceType += 1) {
                if ((bitboards[color][pieceType] & squareMask) != 0) {
                    return new int[]{color, pieceType};
                }
            }
        }

        return null;
    }


    public void addLegalMove(int move) {
        legalMoves[legalMoveCount] = move;
        legalMoveCount += 1;
    }


    // The old moves are still in memory, but they will just get overwritten
    public void clearLegalMoves() {
        legalMoveCount = 0;
    }
}
