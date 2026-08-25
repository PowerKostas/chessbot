package com.chessbot.ui.controllers;

import com.chessbot.engine.core.Piece;
import com.chessbot.ui.components.VisualBoard;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

// This class runs after view.fxml is loaded from ChessApplication, create an HBox to display 2 visual boards if needed
public class MainController {
    @FXML
    private HBox mainContainer;


    public void initialize() {
        VisualBoard boardOne = new VisualBoard(Piece.WHITE, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", false);
        mainContainer.getChildren().addAll(boardOne);
    }
}
