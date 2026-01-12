package com.chessbot.BoardUtils;

import com.chessbot.Objects.Board;
import com.chessbot.Objects.Piece;
import com.chessbot.Objects.Square;
import javafx.scene.Cursor;

// Reads a FEN sequence and places the pieces in the JavaFX board, also returns the updated bitboards
public class FenReader {
    public static void build(String sequence, Board board) {
        int col_num = 0;
        int row_num = 0;
        long bitboard;
        long colourBitboard;
        long allBitboard;

        // Example: First part of the starting FEN looks like this rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
        // A lowercase letter represents a black piece, an uppercase letter represents a white piece
        // Numbers indicate the number of empty squares, slashes indicate new rows
        for (char letter : sequence.toCharArray()) {
            if (Character.isLetter(letter)) {
                Piece piece = Piece.pieceFromFen(letter);

                // Adds the piece to the custom square class, also makes it hoverable
                Square square = (Square) board.getChildren().get(row_num * 8 + col_num);
                square.setCurrentPiece(piece);
                square.setCursor(Cursor.HAND);

                // For the bitboards, colour/piece type, colour, all, adds an 1 to the 64 bit long variable, the 1 is in the
                // position of the piece, eg, piece in the 3rd row and 4th column = bit 27
                bitboard = board.getBitboard(piece.getColour(), piece.getPieceType());
                bitboard += 1L << (row_num * 8 + col_num);
                board.setBitboard(piece.getColour(), piece.getPieceType(), bitboard);

                colourBitboard = board.getOtherBitboard(piece.getColour());
                colourBitboard += 1L << (row_num * 8 + col_num);
                board.setOtherBitboard(piece.getColour(), colourBitboard);

                allBitboard = board.getOtherBitboard(2);
                allBitboard += 1L << (row_num * 8 + col_num);
                board.setOtherBitboard(2, allBitboard);

                col_num += 1;
            }

            else if (Character.isDigit(letter)) {
                col_num += letter - '0';
            }

            else if (letter == '/') {
                col_num = 0;
                row_num += 1;
            }
        }
    }
}
