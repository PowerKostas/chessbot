package com.chessbot.Controllers;

import com.chessbot.BoardUtils.DragMove;
import com.chessbot.Objects.BoardPieces;
import com.chessbot.Objects.Square;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class BoardController {
    @FXML
    private GridPane board;

    private final DragMove dragMove = new DragMove();


    // Prepares every square of the board
    public void initialize() {
        for (int row = 0; row < 8; row += 1) {
            for (int col = 0; col < 8; col += 1) {
                Square square = new Square(row, col, dragMove);
                board.add(square, col, row);
            }
        }
    }


    public void loadGame(BoardPieces boardPieces, String fenSequence) {
        boardPieces.setBoard(fenSequence, board);
    }


    public void loadVisualization(BoardPieces boardPieces) {
        boardPieces.bitboardVisualization(board);
    }
}
