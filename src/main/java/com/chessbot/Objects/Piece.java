package com.chessbot.Objects;

import com.chessbot.ChessApplication;
import com.chessbot.Objects.Pieces.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Piece extends ImageView {
    protected int colour; // White = 0, Black = 1
    protected int pieceType; // King = 0, Pawn = 1, Knight = 2, Bishop = 3, Rook = 4, Queen = 5


    public Piece (int colour, int pieceType, boolean reverse) {
        this.colour = colour;
        this.pieceType = pieceType;

        // Constructor takes int piece parameters and translates them to the String name the images have, then initializes
        // the image
        char[] colourNames = {'w', 'b'};
        char[] pieceNames = {'k', 'p', 'n', 'b', 'r', 'q'};

        Image image = new Image(ChessApplication.class.getResourceAsStream("Images/" + colourNames[colour] + pieceNames[pieceType] + ".png"));
        this.setImage(image);
        this.setFitWidth(75);
        this.setFitHeight(75);

        // If the player is black, reverse the piece image
        if (reverse) {
            this.setRotate(180);
        }
    }


    public int getColour() {
        return colour;
    }

    public int getPieceType() {
        return pieceType;
    }


    // Creates a custom class piece from the FEN letter
    public static Piece pieceFromFen(char fenChar, boolean reverse) {
        int colour = Character.isUpperCase(fenChar) ? 0 : 1; // 0 for white, 1 for black
        char pieceType = Character.toLowerCase(fenChar);

        return switch (pieceType) {
            case 'k' -> new King(colour, reverse);
            case 'p' -> new Pawn(colour, reverse);
            case 'n' -> new Knight(colour, reverse);
            case 'b' -> new Bishop(colour, reverse);
            case 'r' -> new Rook(colour, reverse);
            case 'q' -> new Queen(colour, reverse);
            default -> throw new IllegalArgumentException("Unknown piece: " + fenChar);
        };
    }
}
