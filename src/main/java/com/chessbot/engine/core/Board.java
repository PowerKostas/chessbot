package com.chessbot.engine.core;

import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.utils.FenParser;

import java.util.Arrays;

public class Board {
    private int turn;

    // 12 64 bit variables, one for each piece. The first 6 bitboards are for the white pieces (pawn, knight, bishop, rook,
    // queen, king), the other 6 are for the black pieces. Each bit indicates a square on the board, if the bit equals 0, there
    // is no piece in that square, if it's 1, there is
    private final long[] bitboards = new long[12];

    // 0 = White's bitboard, 1 = Black's bitboard, 2 = All pieces bitboard
    private final long[] otherBitboards = new long[3];

    // 0 = White's attack map for the current turn, = 1 Black's attack map for the current turn
    private final long[] attackMapBitboard = new long[2];

    // 1 for the square that an en passant capture can happen, 0 for everything else
    private long enPassantSquareBitboard = 0L;

    // Holds the legal moves for the current turn. 256 is a safe max limit (the highest number of possible legal moves in any
    // position is 218). The int objects hold info about the legal moves, more information in the Move class
    private final int[] legalMoves = new int[256];

    // Keeps track of how many legal moves are in the array
    private int legalMovesCount = 0;

    // If the friendly king is in check
    private boolean inCheck = false;

    // Holds the castling rights info, 1111 means all castling rights are available
    private int castlingRights = 0b1111;

    // Each constant points to a bit of castling rights
    public static final int WHITE_KINGSIDE = 1;
    public static final int WHITE_QUEENSIDE = 2;
    public static final int BLACK_KINGSIDE = 4;
    public static final int BLACK_QUEENSIDE = 8;

    // Helper mask to update castling rights when pieces move from/to critical squares. All non-critical squares get an initial
    // value of 15 which equals the initial value of castlingRights. The reason for that is: If the player still has castling
    // rights and a move in a non-critical square happens, the operation becomes castlingRights = castlingRights & CASTLING_MASK
    // = 1111 & 1111 = 1111, so the variable remains untouched. If a move in a critical square happens, the appropriate bits
    // of castlingRights are turned off
    private static final int[] CASTLING_MASKS = new int[64];
    static {
        Arrays.fill(CASTLING_MASKS, 15);

        // If the a1 rook moves/gets captured, white queenside castling gets turned off. If the e1 king moves, white
        // kingside/queenside castling gets turned off, the same logic applies for the h1 rook, the a8 rook, the e8 king and
        // the h8 rook
        CASTLING_MASKS[0]  = 15 ^ WHITE_QUEENSIDE;
        CASTLING_MASKS[4]  = 15 ^ (WHITE_KINGSIDE | WHITE_QUEENSIDE);
        CASTLING_MASKS[7]  = 15 ^ WHITE_KINGSIDE;
        CASTLING_MASKS[56] = 15 ^ BLACK_QUEENSIDE;
        CASTLING_MASKS[60] = 15 ^ (BLACK_KINGSIDE | BLACK_QUEENSIDE);
        CASTLING_MASKS[63] = 15 ^ BLACK_KINGSIDE;
    }

    // Counter of half moves since the last capture or pawn push, used in the 50-move rule
    private int halfMoveClock;

    public Board() {}


    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public long getBitboard(int color, int pieceType) { return bitboards[(color * 6) + pieceType]; }

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

    public void setEnPassantSquareBitboard(long bitboard) { this.enPassantSquareBitboard = bitboard; }

    public int getLegalMove(int index) { return legalMoves[index]; }

    public int getLegalMovesCount() { return legalMovesCount; }

    public boolean getInCheck() { return inCheck; }

    public void setInCheck(boolean inCheck) { this.inCheck = inCheck; }

    // Returns the castlingRights bit that is given as a parameter
    public boolean getCastlingRight(int castlingRight) {
        return (this.castlingRights & castlingRight) != 0;
    }

    public void setCastlingRights(int castlingRights) { this.castlingRights = castlingRights; }

    public int getHalfMoveClock() { return halfMoveClock; }

    public void setHalfMoveClock(int halfMoveClock) { this.halfMoveClock = halfMoveClock; }


    // Coordinates every job at the start of the game
    public void loadPosition(String fen) {
        // Loads pieces onto the board and generates moves for the next player
        FenParser.loadFen(fen, this);
        MoveGenerator.generate(this);
    }


    // Adds a piece to the board at the start of the game
    public void addPiece(int pieceColor, int pieceType, int squareIndex) {
        long addMask = 1L << squareIndex;

        bitboards[pieceColor * 6 + pieceType] |= addMask;
        otherBitboards[pieceColor] |= addMask;
        otherBitboards[2] |= addMask;
    }


    // Moves a piece every turn
    public void movePiece(int startingSquare, int endingSquare, int pieceColor, int pieceType) {
        long removeMask = 1L << startingSquare;
        long addMask = 1L << endingSquare;

        bitboards[pieceColor * 6 + pieceType] &= ~removeMask;
        bitboards[pieceColor * 6 + pieceType] |= addMask;

        otherBitboards[pieceColor] &= ~removeMask;
        otherBitboards[pieceColor] |= addMask;

        otherBitboards[2] &= ~removeMask;
        otherBitboards[2] |= addMask;
    }


    // Removes a piece whenever a capture happens
    public void removePiece(int pieceColor, int pieceType, int squareIndex) {
        long removeMask = ~(1L << squareIndex);

        bitboards[pieceColor * 6 + pieceType] &= removeMask;
        otherBitboards[pieceColor] &= removeMask;
        otherBitboards[2] &= removeMask;
    }


    // Coordinates every job of a move cycle
    public void makeMove(int legalMove) {
        // Gets the necessary info about the move
        int startingSquare = Move.getStartingSquare(legalMove);
        int endingSquare = Move.getEndingSquare(legalMove);
        int moveFlag = Move.getFlag(legalMove);

        int pieceColor = this.getPieceColorAtSquare(startingSquare);
        int pieceType = this.getPieceTypeAtSquare(startingSquare);
        int enemyColor = pieceColor ^ 1;

        // Resets the en passant bitboard after each move
        this.enPassantSquareBitboard = 0L;

        // Handles all cases of the move flag
        switch (moveFlag) {
            // If there is no special move flag, just move the piece
            case Move.FLAG_QUIET:
                this.movePiece(startingSquare, endingSquare, pieceColor, pieceType);
                break;

            // If it's a double pawn push, move the pawn and make the square down of the pawn an en passant target, don't
            // worry about the bitwise operation
            case Move.FLAG_DOUBLE_PAWN_PUSH:
                this.movePiece(startingSquare, endingSquare, pieceColor, Piece.PAWN);
                this.enPassantSquareBitboard = 1L << (endingSquare ^ 8);
                break;

            // If it's a normal capture, remove the captured piece from the ending square and move the piece
            case Move.FLAG_CAPTURE:
                this.removePiece(enemyColor, this.getPieceTypeAtSquare(endingSquare), endingSquare);
                this.movePiece(startingSquare, endingSquare, pieceColor, pieceType);
                break;

            // If it's an en passant capture, for white, remove the captured piece from the square that is a rank below the
            // ending square. For black, the captured piece is a rank above the ending square. Then move the pawn
            case Move.FLAG_EN_PASSANT_CAPTURE:
                int capturedPawnSquare = (pieceColor == Piece.WHITE) ? endingSquare - 8 : endingSquare + 8;
                this.removePiece(enemyColor, Piece.PAWN, capturedPawnSquare);
                this.movePiece(startingSquare, endingSquare, pieceColor, Piece.PAWN);
                break;

            // Kingside castling for the white/black king
            case Move.FLAG_KING_CASTLE:
                this.movePiece(startingSquare, endingSquare, pieceColor, pieceType); // King move

                // Rook move
                if (pieceColor == Piece.WHITE) {
                    this.movePiece(7, 5, Piece.WHITE, Piece.ROOK);
                }

                else {
                    this.movePiece(63, 61, Piece.BLACK, Piece.ROOK);
                }

                break;

            // Queenside castling for the white/black king
            case Move.FLAG_QUEEN_CASTLE:
                this.movePiece(startingSquare, endingSquare, pieceColor, pieceType); // King move

                // Rook move
                if (pieceColor == Piece.WHITE) {
                    this.movePiece(0, 3, Piece.WHITE, Piece.ROOK);
                }

                else {
                    this.movePiece(56, 59, Piece.BLACK, Piece.ROOK);
                }

                break;

            // If it's none of the above, it must be a promotion
            default:
                // Because of how move flag is structured, all promotion or promotion capture move flags are after the knight
                // promotion move flag
                if (moveFlag >= Move.FLAG_KNIGHT_PROMOTION) {
                    // All promotion capture move flags are after the knight promotion capture move flag, removes the enemy piece
                    // if it's a promotion capture
                    if (moveFlag >= Move.FLAG_KNIGHT_PROMOTION_CAPTURE) {
                        this.removePiece(enemyColor, this.getPieceTypeAtSquare(endingSquare), endingSquare);
                    }

                    // Remove the pawn from the second to last rank, derive the promoted piece from the 1st and 2nd special bits
                    // of move flag, add the promoted piece to the last rank
                    this.removePiece(pieceColor, Piece.PAWN, startingSquare);

                    int promotedPiece = (moveFlag & 3) + 1;
                    this.addPiece(pieceColor, promotedPiece, endingSquare);
                }

                break;
        }

        // Updates castling rights as described above, the starting square mask is about rook/king moves, the ending square
        // mask is about rooks getting captured
        this.castlingRights &= CASTLING_MASKS[startingSquare] & CASTLING_MASKS[endingSquare];

        // Resets the half move clock if a capture or a pawn push happened, otherwise it's incremented
        if (pieceType == Piece.PAWN || moveFlag == Move.FLAG_CAPTURE || moveFlag == Move.FLAG_EN_PASSANT_CAPTURE) {
            this.halfMoveClock = 0;
        }

        else {
            this.halfMoveClock += 1;
        }

        // Flips the turn and generates moves for the next player
        this.turn ^= 1;
        MoveGenerator.generate(this);
    }


    public void addLegalMove(int move) {
        legalMoves[legalMovesCount] = move;
        legalMovesCount += 1;
    }


    // The old moves are still in memory, but they will just get overwritten
    public void clearLegalMoves() {
        legalMovesCount = 0;
    }


    // Loops through all the legal moves to find a move whose starting and ending squares match the given starting and
    // ending squares
    public int searchLegalMove(int startingSquare, int endingSquare) {
        for (int i = 0; i < legalMovesCount; i++) {
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

        for (int i = 0; i < legalMovesCount; i++) {
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
            if (((bitboards[pieceType] | bitboards[6 + pieceType]) & squareMask) != 0) {
                return pieceType;
            }
        }

        return -1;
    }
}
