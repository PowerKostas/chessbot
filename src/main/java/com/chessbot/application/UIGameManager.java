package com.chessbot.application;

import com.chessbot.engine.ai.Chessbot;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;
import com.chessbot.engine.movegen.utils.Checks;
import com.chessbot.engine.utils.ResultDetector;
import com.chessbot.ui.components.VisualBoard;
import com.chessbot.ui.utils.SoundManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

// This class exists to centralize the move execution logic for UI-based games. It ensures that all the necessary game actions
// are performed here regardless of if the input is coming from the UI, the engine or anything else. This way there is no need to
// write extra code on any specific input class
public class UIGameManager {
    private Board board;
    private final VisualBoard visualBoard;

    // Pre-allocates one move list object for the entire lifecycle of the UI
    private final MoveList moveList = new MoveList();

    private boolean inCheck;

    // If white/black is human/engine
    private int whitePlayerType;
    private int blackPlayerType;

    // Pluggable bots of different engine versions for white and black
    private Chessbot whiteBot = ApplicationConfig.INITIAL_WHITE_BOT;
    private Chessbot blackBot = ApplicationConfig.INITIAL_BLACK_BOT;

    // In endless mode when a game ends another one gets setup, instead of the window closing
    private final boolean isEndlessMode;


    public UIGameManager(Board board, VisualBoard visualBoard, boolean isEndlessMode) {
        this.board = board;
        this.visualBoard = visualBoard;
        this.isEndlessMode = isEndlessMode;
    }


    public MoveList getMoveList() { return moveList; }


    // Initializes the class variables and decides what to do in the first turn
    public void startGame(int whitePlayerType, int blackPlayerType) {
        this.whitePlayerType = whitePlayerType;
        this.blackPlayerType = blackPlayerType;
        MoveGenerator.generate(board, moveList);
        inCheck = (Checks.calculateSquares(board, board.getTurn()) != 0L);

        // If 2 engines are playing, a message is needed to differentiate them because they can swap sides after each game
        if (ApplicationConfig.WHITE_PLAYER_TYPE == PlayerType.ENGINE && ApplicationConfig.BLACK_PLAYER_TYPE == PlayerType.ENGINE) {
            System.out.printf("White: %s | Black: %s%n%n", whiteBot.name(), blackBot.name());
        }

        // Have to check if the loaded FEN is an already completed game
        if (getGameResult()) {
            triggerGameOverSequence();
        }

        else {
            checkTurn();
        }
    }


    // If it's the engine's turn, lock the board so humans can't move pieces and then play the chosen move of the engine. If it's the
    // human's turn, unlock the board for input
    private void checkTurn() {
        int currentTurn = board.getTurn();
        int currentPlayerType = (currentTurn == Piece.WHITE) ? whitePlayerType : blackPlayerType;

        if (currentPlayerType == PlayerType.ENGINE) {
            visualBoard.setIsBoardLocked(true);
            Chessbot currentBot = (currentTurn == Piece.WHITE) ? whiteBot : blackBot;

            // Adds a small delay before the engine moves to make the game seem natural. Runs the search on a background thread
            // in order for the window to not freeze when the engine is thinking
            PauseTransition pause = new PauseTransition(Duration.millis(600));

            pause.setOnFinished(_ ->
                new Thread(() -> {
                    var move = currentBot.chooseMove(board);
                    Platform.runLater(() -> playMove(move));
                }).start()
            );

            pause.play();
        }

        else {
            visualBoard.setIsBoardLocked(false);
        }
    }


    // Handles human and engine moves
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


    private boolean getGameResult() {
        return ResultDetector.isCheckmate(moveList, inCheck) || ResultDetector.isStalemate(moveList, inCheck) ||
               ResultDetector.isFiftyMoveRule(board) || ResultDetector.isInsufficientMaterial(board) ||
               ResultDetector.isThreefoldRepetition(board);
    }


    private void triggerGameOverSequence() {
        visualBoard.setDisable(true);
        SoundManager.playEndSound();

        PauseTransition delay = new PauseTransition(Duration.seconds(5));

        if (isEndlessMode) {
            delay.setOnFinished(_ -> restartGame());
        }

        else {
            delay.setOnFinished(_ -> Platform.exit());
        }

        delay.play();
    }


    private void restartGame() {
        // Swaps colors within player types and within bots
        int tempPlayerType = whitePlayerType;
        whitePlayerType = blackPlayerType;
        blackPlayerType = tempPlayerType;

        Chessbot tempBot = whiteBot;
        whiteBot = blackBot;
        blackBot = tempBot;

        // Resets the engine board
        board = new Board();
        board.loadInitialPosition(ApplicationConfig.ENDLESS_MODE_FEN);

        // Resets the visual board, the board is flipped if there is only one human player and they are black
        visualBoard.setDisable(false);
        visualBoard.setBoard(board);
        int boardPerspective = (whitePlayerType != blackPlayerType && blackPlayerType == PlayerType.HUMAN) ? Piece.BLACK : Piece.WHITE;
        visualBoard.flip(boardPerspective);
        visualBoard.unhighlightPreviousMove();
        visualBoard.sync(null);

        startGame(whitePlayerType, blackPlayerType);
    }
}
