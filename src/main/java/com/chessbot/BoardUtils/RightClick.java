package com.chessbot.BoardUtils;

import com.chessbot.Objects.Square;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class RightClick {
    public void click(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) { // if it's a right click
            Square clickedSquare = (Square) event.getSource();

            // If it's not right-clicked, give it right-clicked colour
            if (!clickedSquare.getIsRightClicked()) {
                clickedSquare.setStyle("-fx-background-color: #eb7d6a", "-fx-background-color: #d36c50");
                clickedSquare.setIsRightClicked(true);
            }

            // If it is right-clicked and selected, give back selected colour
            else if (clickedSquare.getIsSelected()) {
                clickedSquare.setStyle("-fx-background-color: #f5f682", "-fx-background-color: #b9ca43");
                clickedSquare.setIsRightClicked(false);

            }

            // If it is right-clicked and default, give back default colour
            else {
                clickedSquare.setStyle("-fx-background-color: #ebecd0", "-fx-background-color: #739552");
                clickedSquare.setIsRightClicked(false);
            }
        }
    }
}
