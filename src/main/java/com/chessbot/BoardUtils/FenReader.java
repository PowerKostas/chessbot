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
        int bitboardIndex;
        long bitboard;

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

                bitboardIndex = 6 * piece.getColour() + piece.getPieceType(); // See Board bitboards to understand
                bitboard = board.getBitboard(bitboardIndex);

                // Adds an 1 to the 64 bit long variable, the 1 is in the position of the piece
                // eg, piece in the 3rd row and 4th column = bit 27
                bitboard += 1L << (row_num * 8 + col_num);
                board.setBitboard(bitboardIndex, bitboard);

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
