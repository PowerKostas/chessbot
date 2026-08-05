package com.chessbot.ui.components;

import com.chessbot.ChessApplication;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VisualPiece extends ImageView {
    private final int color;
    private final int type;


    public VisualPiece(int color, int type, boolean reverse) {
        this.color = color;
        this.type = type;

        // Constructor takes int piece parameters and translates them to the String name the images have, then initializes
        // the image
        char[] colorNames = {'w', 'b'};
        char[] pieceNames = {'p', 'n', 'b', 'r', 'q', 'k'};

        Image image = new Image(ChessApplication.class.getResourceAsStream("Images/" + colorNames[color] + pieceNames[type] + ".png"));
        this.setImage(image);
        this.setFitWidth(75);
        this.setFitHeight(75);

        // If the player is black, reverse the piece image
        if (reverse) {
            this.setRotate(180);
        }
    }


    public int getColor() { return color; }

    public int getType() { return type; }
}
