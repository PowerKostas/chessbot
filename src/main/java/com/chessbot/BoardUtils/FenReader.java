package com.chessbot.BoardUtils;

import com.chessbot.BitboardUtils.SetBitboards;
import com.chessbot.Objects.Board;
import com.chessbot.Objects.Piece;
import com.chessbot.Objects.Square;
import javafx.scene.Cursor;

// Reads a FEN sequence and places the pieces in the JavaFX board, also updates the bitboards
public class FenReader {
    public static void build(String fen, Board board) {
        int col_num = 0;
        int row_num = 0;

        // Example: First part of the starting FEN looks like this rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
        // A lowercase letter represents a black piece, an uppercase letter represents a white piece
        // Numbers indicate the number of empty squares, slashes indicate new rows
        for (char letter : fen.toCharArray()) {
            if (Character.isLetter(letter)) {
                // If the player is black, reverse the piece image
                Piece piece;
                if (board.getPlayerColour() == 1) {
                    piece = Piece.pieceFromFen(letter, true);
                }

                else {
                    piece = Piece.pieceFromFen(letter, false);
                }

                // Adds the piece to the custom square class, also makes it hoverable
                Square square = (Square) board.getChildren().get(row_num * 8 + col_num);
                square.setCurrentPiece(piece);
                square.setCursor(Cursor.HAND);

                // Have to reverse back the bitboard square indexes because the JavaFX bitboard is reversed (starts from the top left, instead of the bottom left)
                SetBitboards.start(board, piece.getColour(), piece.getPieceType(), (7 - row_num) * 8 + col_num);

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

        board.generateOpponentPseudoLegalMoves(board.getTurn() ^ 1);
    }
}
