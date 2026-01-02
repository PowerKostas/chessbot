package com.chessbot.ChessUtils;

import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class MakeMove {
    private static double mouseAnchorX;
    private static double mouseAnchorY;
    private static StackPane startingSquare;
    private static String startingSquareStyle;
    private static String hoveredSquareStyle;

    public static void dragDetected(MouseEvent event, GridPane board) {
        mouseAnchorX = event.getSceneX();
        mouseAnchorY = event.getSceneY();

        startingSquare = (StackPane) event.getSource();
        startingSquareStyle = startingSquare.getStyle();
        startingSquare.setStyle("-fx-background-color: #b9ca43");
        startingSquare.setViewOrder(-1);
    }

    public static void drag(MouseEvent event) {
        StackPane startingSquare = (StackPane) event.getSource();
        ImageView draggedPiece = (ImageView) startingSquare.getChildren().getLast();

        double deltaX = event.getSceneX() - mouseAnchorX;
        double deltaY = event.getSceneY() - mouseAnchorY;

        draggedPiece.setTranslateX(deltaX);
        draggedPiece.setTranslateY(deltaY);
    }

    public static void dragEnter(MouseEvent event) {
        StackPane hoveredSquare = (StackPane) event.getSource();
        hoveredSquareStyle = hoveredSquare.getStyle();
        hoveredSquare.setStyle(hoveredSquareStyle + "; -fx-border-color: #f8f8ef; -fx-border-width: 4");
    }

    public static void dragExit(MouseEvent event) {
        StackPane hoveredSquare = (StackPane) event.getSource();
        hoveredSquare.setStyle(hoveredSquareStyle);
    }

    public static void dragRelease(MouseEvent event) {
        ImageView draggedPiece = (ImageView) startingSquare.getChildren().getLast();
        draggedPiece.setTranslateX(0);
        draggedPiece.setTranslateY(0);
        startingSquare.getChildren().remove(draggedPiece);
        startingSquare.setStyle(startingSquareStyle);
        startingSquare.setViewOrder(0);

        StackPane endingSquare = (StackPane) event.getSource();
        endingSquare.getChildren().add(draggedPiece);
    }
}
