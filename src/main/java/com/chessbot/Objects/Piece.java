package com.chessbot.Objects;

import com.chessbot.ChessApplication;
import com.chessbot.Objects.Pieces.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Piece {
    private ImageView pieceImage;
    protected int colour; // White = 0, Black = 1
    protected int pieceType; // King = 0, Pawn = 1, Knight = 2, Bishop = 3, Rook = 4, Queen = 5


    public Piece (int colour, int pieceType) {
        this.colour = colour;
        this.pieceType = pieceType;

        // Constructor takes int piece parameters and translates them to the String name the images have, then initializes
        // the image
        char[] colourNames = {'w', 'b'};
        char[] pieceNames = {'k', 'p', 'n', 'b', 'r', 'q'};

        Image image = new Image(ChessApplication.class.getResourceAsStream("Images/" + colourNames[colour] + pieceNames[pieceType] + ".png"));
        this.pieceImage = new ImageView(image);
        this.pieceImage.setFitWidth(75);
        this.pieceImage.setFitHeight(75);
    }


    public ImageView getPieceImage() {
        return pieceImage;
    }

    public int getColour() {
        return colour;
    }

    public int getPieceType() {
        return pieceType;
    }


    // Creates a custom class piece from the FEN letter
    public static Piece pieceFromFen(char fenChar) {
        int colour = Character.isUpperCase(fenChar) ? 0 : 1; // 0 for white, 1 for black
        char pieceType = Character.toLowerCase(fenChar);

        return switch (pieceType) {
            case 'k' -> new King(colour);
            case 'p' -> new Pawn(colour);
            case 'n' -> new Knight(colour);
            case 'b' -> new Bishop(colour);
            case 'r' -> new Rook(colour);
            case 'q' -> new Queen(colour);
            default -> throw new IllegalArgumentException("Unknown piece: " + fenChar);
        };
    }
}
