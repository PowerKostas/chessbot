package com.chessbot.ui.controllers;

import com.chessbot.application.GameManager;
import com.chessbot.application.PlayerType;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;
import com.chessbot.ui.components.VisualBoard;
import com.chessbot.ui.input.MoveHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

// This class runs after view.fxml is loaded from ChessApplication, it's responsible for setting up the game for the UI
public class MainController {
    // Creates an HBox to display 2 visual boards if needed
    @FXML
    private HBox mainContainer;

    // If white/black is human or AI
    int whitePlayerType = PlayerType.HUMAN;
    int blackPlayerType = PlayerType.HUMAN;


    public void initialize() {
        // If there is only one human player, and they are black, set the board's perspective to black pieces first
        int boardPerspective = Piece.WHITE;
        if (whitePlayerType != blackPlayerType && blackPlayerType == PlayerType.HUMAN) boardPerspective = Piece.BLACK;

        // Creates the visual board which in turn creates the engine board
        VisualBoard boardOne = new VisualBoard(boardPerspective, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", false);
        mainContainer.getChildren().addAll(boardOne);
        Board board = boardOne.getBoard();

        // Initializes the game manager and move handler objects and attaches the external move handler to the visual board
        GameManager gameManager = new GameManager(board, boardOne);
        MoveHandler moveHandler = new MoveHandler(boardOne, gameManager::playMove);
        boardOne.attachMoveHandler(moveHandler);

        // Sets the game's type and starts it
        gameManager.startGame(whitePlayerType, blackPlayerType);
    }
}
