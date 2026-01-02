package com.chessbot.Utils;

import com.chessbot.ChessApplication;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class FENReader {
    public static void build(String sequence, GridPane board) {
        int col_num = 0;
        int row_num = 0;

        // Example: First part of the starting FEN looks like this rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
        // A lowercase letter represents a black piece, an uppercase letter represents a white piece
        // Numbers indicate the number of empty squares, slashes indicate new rows
        for (char letter : sequence.toCharArray()) {
            if (Character.isLetter(letter)) {
                if (Character.isLowerCase(letter)) {
                    Image image = new Image(ChessApplication.class.getResourceAsStream("Images/b" + letter +".png"));
                    ImageView piece = new ImageView(image);
                    piece.setFitWidth(75);
                    piece.setFitHeight(75);

                    StackPane square = new StackPane();
                    square.getChildren().add(piece);
                    board.add(square, col_num, row_num);
                }

                else {
                    Image image = new Image(ChessApplication.class.getResourceAsStream("Images/w" + Character.toLowerCase(letter) + ".png"));
                    ImageView piece = new ImageView(image);
                    piece.setFitWidth(75);
                    piece.setFitHeight(75);

                    StackPane square = new StackPane();
                    square.getChildren().add(piece);
                    board.add(square, col_num, row_num);
                }

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
