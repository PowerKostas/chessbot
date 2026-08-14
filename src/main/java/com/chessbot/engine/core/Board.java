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


    // Coordinates every job at the start of the game
    public void loadPosition(String fen) {
        // Loads pieces onto the board and generates moves for the next player
        FenParser.loadFen(fen, this);
        MoveGenerator.generate(this);
    }


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

        int pieceColor = this.getPieceColorAtSquare(startingSquare);
        int pieceType = this.getPieceTypeAtSquare(startingSquare);
        int enemyColor = pieceColor ^ 1;

        // Removes a piece if necessary, if a normal capture happened, the captured piece is in the ending square
        if (flag == Move.FLAG_CAPTURE) {
            this.removePiece(enemyColor, this.getPieceTypeAtSquare(endingSquare), endingSquare);
        }

        // If an en passant capture happened, for white the captured pawn is a rank below the ending square, for black the
        // captured pawn is a rank above the ending square
        else if (flag == Move.FLAG_EN_PASSANT_CAPTURE) {
            int capturedPawnSquare = (pieceColor == Piece.WHITE) ? endingSquare - 8 : endingSquare + 8;
            this.removePiece(enemyColor, Piece.PAWN, capturedPawnSquare);
        }

        // Moves the piece
        this.movePiece(startingSquare, endingSquare, pieceColor, pieceType);

        // Resets the en passant bitboard after each move, unless a double pawn push happened, then the square 1 down of the
        // pawn is an en passant target, don't worry about the bitwise operation
        if (flag == Move.FLAG_DOUBLE_PAWN_PUSH) {
            this.enPassantSquareBitboard = 1L << (endingSquare ^ 8);
        }

        else {
            this.enPassantSquareBitboard = 0L;
        }

        System.out.println(flag);

        // Flips the turn and generates moves for the next player
        this.turn ^= 1;
        MoveGenerator.generate(this);
    }


    public void addLegalMove(int move) {
        legalMoves[legalMoveCount] = move;
        legalMoveCount += 1;
    }


    // The old moves are still in memory, but they will just get overwritten
    public void clearLegalMoves() {
        legalMoveCount = 0;
    }


    // Loops through all the legal moves to find a move whose starting and ending squares match the given starting and
    // ending squares
    public int searchLegalMove(int startingSquare, int endingSquare) {
        for (int i = 0; i < legalMoveCount; i++) {
            int legalMove = legalMoves[i];
            if (Move.getStartingSquare(legalMove) == startingSquare && Move.getEndingSquare(legalMove) == endingSquare) {
                return legalMove;
            }
        }

        return -1;
    }


    // Loops through all the legal moves to find moves whose starting square matches the given starting square, used to find
    // all the piece's legal moves
    public long searchPieceLegalMoves(int startingSquare) {
        long pieceLegalMovesBitboard = 0L;

        for (int i = 0; i < legalMoveCount; i++) {
            int move = legalMoves[i];
            if (Move.getStartingSquare(move) == startingSquare) {
                int endingSquare = Move.getEndingSquare(move);
                pieceLegalMovesBitboard |= (1L << endingSquare);
            }
        }

        return pieceLegalMovesBitboard;
    }


    // Gets a piece's color at a specific square
    public int getPieceColorAtSquare(int squareIndex) {
        long squareMask = 1L << squareIndex;

        if ((otherBitboards[Piece.WHITE] & squareMask) != 0) {
            return Piece.WHITE;
        }

        if ((otherBitboards[Piece.BLACK] & squareMask) != 0) {
            return Piece.BLACK;
        }

        return -1;
    }


    // Gets a piece's type at a specific square
    public int getPieceTypeAtSquare(int squareIndex) {
        long squareMask = 1L << squareIndex;

        // Checks if either white or black has that piece type on that square
        for (int pieceType = 0; pieceType < 6; pieceType += 1) {
            if (((bitboards[Piece.WHITE][pieceType] | bitboards[Piece.BLACK][pieceType]) & squareMask) != 0) {
                return pieceType;
            }
        }

        return -1;
    }
}
