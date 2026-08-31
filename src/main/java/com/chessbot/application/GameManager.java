package com.chessbot.application;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.movegen.Checks;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;
import com.chessbot.engine.utils.ResultDetector;
import com.chessbot.ui.components.VisualBoard;
import com.chessbot.ui.utils.SoundManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.Random;

// This class exists to centralize the move execution logic. It ensures that all the necessary game actions are performed here
// regardless of if the input is coming from the UI, an AI or anything else. This way there is no need to write extra code on any
// specific input class
public class GameManager {
    private final Board board;
    private final VisualBoard visualBoard;
    private final Random random = new Random();

    // If white/black is human or AI
    private int whitePlayerType;
    private int blackPlayerType;

    // Pre-allocates one move list object for the entire lifecycle of the UI
    private final MoveList moveList = new MoveList();

    private boolean inCheck;


    public GameManager(Board board, VisualBoard visualBoard) {
        this.board = board;
        this.visualBoard = visualBoard;
    }


    public MoveList getMoveList() { return moveList; }


    // Initializes the class variables and decides what to do in the first turn
    public void startGame(int whitePlayerType, int blackPlayerType) {
        this.whitePlayerType = whitePlayerType;
        this.blackPlayerType = blackPlayerType;
        MoveGenerator.generate(board, moveList);
        inCheck = (Checks.calculateSquares(board, board.getTurn()) != 0L);
        checkTurn();
    }


    // If it's the AI's turn, lock the board so humans can't move pieces and then play a move. If it's the human's turn, unlock the
    // board for input
    private void checkTurn() {
        int currentTurn = board.getTurn();
        int currentPlayerType = (currentTurn == Piece.WHITE) ? whitePlayerType : blackPlayerType;

        if (currentPlayerType == PlayerType.AI) {
            visualBoard.setIsBoardLocked(true);

            // Adds a small delay to make the AI moves seem natural
            PauseTransition pause = new PauseTransition(Duration.millis(600));
            pause.setOnFinished(e -> playRandomAIMove());
            pause.play();
        }

        else {
            visualBoard.setIsBoardLocked(false);
        }
    }


    private boolean getGameResult() {
        return ResultDetector.isCheckmate(moveList, inCheck) || ResultDetector.isStalemate(moveList, inCheck) || ResultDetector.isFiftyMoveRule(board) || ResultDetector.isInsufficientMaterial(board);
    }


    // If the game ended, lock the board, play the end sound and close the window after 5 seconds
    private void triggerGameOverSequence() {
        visualBoard.setIsBoardLocked(true);
        SoundManager.playEndSound();

        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(e -> Platform.exit());
        delay.play();
    }


    // Handles human and AI moves
    public void playMove(int legalMove) {
        board.makeMove(legalMove);

        // Updates the class variables after the move was made
        MoveGenerator.generate(board, moveList);
        inCheck = (Checks.calculateSquares(board, board.getTurn()) != 0L);

        visualBoard.sync(moveList);

        int startingSquare = Move.getStartingSquare(legalMove);
        int endingSquare = Move.getEndingSquare(legalMove);
        visualBoard.highlightPreviousMove(startingSquare, endingSquare);

        // Checks if the game has ended in any way, if yes perform the necessary actions, else just play the appropriate move
        // sound and decide what to do in the next turn
        if (getGameResult()) {
            triggerGameOverSequence();
        }

        else {
            SoundManager.playMoveSound(inCheck, Move.getFlag(legalMove));
            checkTurn();
        }
    }


    // Handles AI moves
    private void playRandomAIMove() {
        // Picks and plays a random legal move
        int legalMovesCount = moveList.count;
        int randomLegalMove = moveList.moves[random.nextInt(legalMovesCount)];
        playMove(randomLegalMove);
    }
}
