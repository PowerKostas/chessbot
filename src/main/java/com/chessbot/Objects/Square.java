package com.chessbot.Objects;

import com.chessbot.BoardUtils.DragMove;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class Square extends StackPane {
    private int row;
    private int col;


    public Square(int row, int col, DragMove dragMove) {
        // Adds square colour
        if ((row + col) % 2 == 0) {
            this.setStyle("-fx-background-color: #ebecd0");
        }

        else {
            this.setStyle("-fx-background-color: #739552");
        }

        // Adds number coordinates
        if (col == 0) {
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
            StackPane.setMargin(number, new Insets(0, 0, 0, 4));

            this.getChildren().add(number);
        }

        // Adds letter coordinates
        if (row == 7) {
            Label number = new Label();

            if (col % 2 == 0) {
                number.setTextFill(Color.web("#ebecd0"));
            } else {
                number.setTextFill(Color.web("#739552"));
            }

            number.setText(String.valueOf((char) ('a' + col)));
            number.setStyle("-fx-font-size: 16; -fx-font-weight: bold");
            StackPane.setAlignment(number, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(number, new Insets(0, 4, 0, 0));

            this.getChildren().add(number);
        }

        // Makes every square draggable/clickable, more info on the specific classes
        this.setOnDragDetected(event -> {
            dragMove.dragDetected(event);
        });

        this.setOnDragOver(event -> {
            dragMove.drag(event);
        });

        this.setOnDragEntered(event -> {
            dragMove.dragEnter(event);
        });

        this.setOnDragDropped(event -> {
            dragMove.dragDropped(event);
        });

        this.setOnDragExited(event -> {
            dragMove.dragExit(event);
        });

        this.setOnDragDone(event -> {
            dragMove.dragDone(event);
        });
    }
}
