package com.chessbot.engine.core;

import com.chessbot.engine.movegen.PseudoLegalMoves;
import com.chessbot.engine.utils.FenParser;

public class Board {
    // 0 = White, 1 = Black
    private int turn = 0;

    // 12 64 bit variables, one for each piece color and piece type, first dimension is the piece color and second dimension
    // is the piece type, each bit indicates a piece on the board, used for board representation
    private final long[][] bitboards = new long[2][6];

    // 0 = White bitboard, 1 = Black bitboard, 2 = All bitboard
    private final long[] otherBitboards = new long [3];

    private long allPseudoLegalMovesBitboard;

    // 1 for the square that an en passant capture can happen, 0 for everything else
    private long enPassantSquareBitboard = 0L;

    public Board() { }


    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public long getBitboard(int color, int pieceType) {
        return bitboards[color][pieceType];
    }

    public void setBitboard(int color, int pieceType, long bitboard) {
        this.bitboards[color][pieceType] = bitboard;
    }

    public long getOtherBitboard(int index) {
        return otherBitboards[index];
    }

    public void setOtherBitboard(int index, long bitboard) {
        this.otherBitboards[index] = bitboard;
    }

    public long getAllPseudoLegalMovesBitboard() {
        return allPseudoLegalMovesBitboard;
    }

    public void setAllPseudoLegalMovesBitboard(long allPseudoLegalMovesBitboard) { this.allPseudoLegalMovesBitboard = allPseudoLegalMovesBitboard; }

    public long getEnPassantSquareBitboard() {
        return enPassantSquareBitboard;
    }

    public void setEnPassantSquareBitboard(long enPassantSquareBitboard) {this.enPassantSquareBitboard = enPassantSquareBitboard; }


    // Adds a piece to the board at the start of the game
    public void addPiece(int pieceColor, int pieceType, int squareIndex) {
        long addMask = 1L << squareIndex;

        bitboards[pieceColor][pieceType] |= addMask;
        otherBitboards[pieceColor] |= addMask;
        otherBitboards[2] |= addMask;
    }


    // Moves a piece every turn
    public void movePiece(int pieceColor, int pieceType, int oldSquareIndex, int newSquareIndex) {
        long removeMask = 1L << oldSquareIndex;
        long addMask = 1L << newSquareIndex;

        bitboards[pieceColor][pieceType] &= ~removeMask;
        bitboards[pieceColor][pieceType] |= addMask;

        otherBitboards[pieceColor] &= ~removeMask;
        otherBitboards[pieceColor] |= addMask;

        otherBitboards[2] &= ~removeMask;
        otherBitboards[2] |= addMask;

        // Resets the en passant bitboard after each move, and if a pawn moved 2 squares up, the square 1 up is an en passant
        // target, don't worry about the bitwise operations, they work
        if (pieceType == 1 && (newSquareIndex ^ oldSquareIndex) == 16) {
            enPassantSquareBitboard = 1L << (newSquareIndex ^ 8);
        }

        else {
            enPassantSquareBitboard = 0L;
        }
    }


    // Coordinates every job of a move cycle
    public void makeMove(int color, int pieceType, int startSquare, int endSquare) {
        this.movePiece(color, pieceType, startSquare, endSquare);

        this.turn ^= 1;
        PseudoLegalMoves.generate(this, color);
    }


    // Coordinates every job at the start of the game
    public void loadPosition(String fen) {
        FenParser.loadFen(fen, this);
        PseudoLegalMoves.generate(this, this.turn ^ 1);
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
}
