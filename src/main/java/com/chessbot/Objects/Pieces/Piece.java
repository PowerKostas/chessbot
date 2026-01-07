package com.chessbot.Objects.Pieces;

import com.chessbot.ChessApplication;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Piece {
    private ImageView pieceImage;
    protected int colour; // White = 0, Black = 1
    protected int pieceType; // King = 0, Pawn = 1, Knight = 2, Bishop = 3, Rook = 4, Queen = 5


    public Piece(int colour, int pieceType) {
        this.colour = colour;
        this.pieceType = pieceType;

        // Constructor takes int piece parameters and translates them to the String name the images have, then initializes
        // the image
        char[] pieceNames = {'k', 'p', 'n', 'b', 'r', 'q'};
        char[] colourNames = {'w', 'b'};

        Image image = new Image(ChessApplication.class.getResourceAsStream("Images/" + colourNames[colour] + pieceNames[pieceType] + ".png"));
        this.pieceImage = new ImageView(image);
        this.pieceImage.setFitWidth(75);
        this.pieceImage.setFitHeight(75);
    }


    public int getColour() {
        return colour;
    }

    public int getPieceType() {
        return pieceType;
    }
}
