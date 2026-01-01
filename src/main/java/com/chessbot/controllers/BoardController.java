package com.chessbot.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

public class BoardController {
    @FXML
    private GridPane board;


    // Prepares every square of the board
    public void initialize() {
        for (int row = 0; row < 8; row += 1) {
            for (int column = 0; column < 8; column += 1) {
                StackPane square = new StackPane();

                // Adds number coordinates
                if (column == 0) {
                    Label number = new Label();

                    if (row % 2 == 0) {
                        number.setTextFill(Color.web("#739552"));
                    }

                    else {
                        number.setTextFill(Color.web("#ebecd0"));
                    }

                    number.setText(Integer.toString(8 - row));
                    number.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
                    StackPane.setAlignment(number, Pos.TOP_LEFT);
                    StackPane.setMargin(number, new Insets(0, 0, 0, 5));

                    square.getChildren().add(number);
                }

                // Adds letter coordinates
                if (row == 7) {
                    Label number = new Label();

                    if (column % 2 == 0) {
                        number.setTextFill(Color.web("#ebecd0"));
                    } else {
                        number.setTextFill(Color.web("#739552"));
                    }

                    char fileLetter = (char)('a' + column);
                    number.setText(String.valueOf(fileLetter));
                    number.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
                    StackPane.setAlignment(number, Pos.BOTTOM_RIGHT);
                    StackPane.setMargin(number, new Insets(0, 5, 0, 0));

                    square.getChildren().add(number);
                }

                // Adds square colour
                if ((row + column) % 2 == 0) {
                    square.setStyle("-fx-background-color: #ebecd0");
                }

                else {
                    square.setStyle("-fx-background-color: #739552");
                }

                board.add(square, column, row);
            }
        }
    }
}
