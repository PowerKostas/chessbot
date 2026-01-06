package com.chessbot.BoardUtils;

import com.chessbot.ChessApplication;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

// Reads a FEN sequence and places the pieces in the JavaFX board, also returns the updated bitboards
public class FenReader {
    public long build(String sequence, GridPane board) {
        long allPiecesBitboard = 0;
        int col_num = 0;
        int row_num = 0;

        // Example: First part of the starting FEN looks like this rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
        // A lowercase letter represents a black piece, an uppercase letter represents a white piece
        // Numbers indicate the number of empty squares, slashes indicate new rows
        for (char letter : sequence.toCharArray()) {
            if (Character.isLetter(letter)) {
                ImageView piece;

                if (Character.isLowerCase(letter)) {
                    Image image = new Image(ChessApplication.class.getResourceAsStream("Images/b" + letter +".png"));
                    piece = new ImageView(image);
                    piece.setFitWidth(75);
                    piece.setFitHeight(75);
                }

                else {
                    Image image = new Image(ChessApplication.class.getResourceAsStream("Images/w" + Character.toLowerCase(letter) + ".png"));
                    piece = new ImageView(image);
                    piece.setFitWidth(75);
                    piece.setFitHeight(75);
                }

                // Adds piece and makes it hoverable
                StackPane square = (StackPane) board.getChildren().get(row_num * 8 + col_num);
                square.getChildren().add(piece);
                square.setCursor(Cursor.HAND);

                // Adds an 1 to the 64 bit long variable, the 1 is in the position of the piece
                // eg, piece in the 3rd row and 4th column = bit 27
                allPiecesBitboard += 1L << (row_num * 8 + col_num);

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

        return allPiecesBitboard;
    }
}
