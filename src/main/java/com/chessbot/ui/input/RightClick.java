package com.chessbot.ui.input;

import com.chessbot.ui.components.Square;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class RightClick {
    public void mouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) { // If it's a right click
            Square clickedSquare = (Square) event.getSource();

            // If it's not right-clicked, give right-clicked color
            if (!clickedSquare.getIsRightClicked()) {
                clickedSquare.setStyle("-fx-background-color: #eb7d6a", "-fx-background-color: #d36c50");
                clickedSquare.setIsRightClicked(true);
            }

            // If it's right-clicked, give default color
            else {
                clickedSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
                clickedSquare.setIsRightClicked(false);
            }
        }
    }
}
