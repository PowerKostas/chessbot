package com.chessbot.Controllers;

import com.chessbot.Objects.BoardPieces;
import javafx.fxml.FXML;

public class TwoBoardsController {
    @FXML
    private BoardController boardOneController;

    @FXML
    private BoardController boardTwoController;

    private final BoardPieces boardPieces = new BoardPieces();


    public void initialize() {
        boardOneController.loadGame(boardPieces, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

        callBitboardVisualization();

        boardTwoController.loadGame(boardPieces, "8/8/8/8/8/8/8/8");
    }


    public void callBitboardVisualization() {
        boardTwoController.loadVisualization(boardPieces);
    }
}
