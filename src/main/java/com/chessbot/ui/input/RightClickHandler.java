package com.chessbot.ui.input;

import com.chessbot.ui.components.Square;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class RightClickHandler {
    public void mouseClicked(MouseEvent event) {
        // If it's a right click, toggle the square's right-clicked status
        if (event.getButton() == MouseButton.SECONDARY) {
            Square clickedSquare = (Square) event.getSource();
            clickedSquare.setIsRightClicked(!clickedSquare.getIsRightClicked());
        }
    }
}
