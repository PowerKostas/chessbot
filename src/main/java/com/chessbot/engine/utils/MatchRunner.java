package com.chessbot.engine.utils;

import com.chessbot.application.ApplicationConfig;
import com.chessbot.engine.ai.Chessbot;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;
import com.chessbot.engine.movegen.utils.Checks;

// Runs a set amount of games between 2 versions of the engine in order to check if a change in the evaluation/search function had
// positive or negative impact
public class MatchRunner {
    static void main() {
        MoveList moveList = new MoveList();
        Chessbot initialBlackBot = ApplicationConfig.INITIAL_BLACK_BOT;
        Chessbot initialWhiteBot = ApplicationConfig.INITIAL_WHITE_BOT;

        int gamesToPlay = 100;
        int currentVersionBotWins = 0, previousVersionBotWins = 0, draws = 0;

        System.out.printf("%d-game match | %s vs %s%n%n", gamesToPlay, initialBlackBot.name(), initialWhiteBot.name());

        long startTime = System.nanoTime();

        for (int game = 1; game <= gamesToPlay; game += 1) {
            Board board = new Board();
            board.loadInitialPosition(ApplicationConfig.MATCH_RUNNER_FEN);

            // Swaps the bot's colors each game
            boolean isEvenGame = (game % 2 == 0);
            Chessbot whiteBot = isEvenGame ? initialBlackBot : initialWhiteBot;
            Chessbot blackBot = isEvenGame ? initialWhiteBot : initialBlackBot;

            while (true) {
                MoveGenerator.generate(board, moveList);
                boolean inCheck = Checks.calculateSquares(board, board.getTurn()) != 0L;

                if (ResultDetector.isCheckmate(moveList, inCheck)) {
                    // Because the turn changes after every move, if there is checkmate, and it's black's turn, it means that
                    // white is the one that delivered the checkmate
                    boolean whiteWon = (board.getTurn() == Piece.BLACK);

                    if (whiteWon == isEvenGame) {
                        previousVersionBotWins += 1;
                    }

                    else {
                        currentVersionBotWins += 1;
                    }

                    break;
                }

                if (ResultDetector.isStalemate(moveList, inCheck) || ResultDetector.isFiftyMoveRule(board) ||
                    ResultDetector.isInsufficientMaterial(board) || ResultDetector.isThreefoldRepetition(board)
                ) {
                    draws += 1;
                    break;
                }

                Chessbot activeBot = (board.getTurn() == Piece.WHITE) ? whiteBot : blackBot;
                board.makeMove(activeBot.chooseMove(board));
            }

            System.out.printf("Game %d/%d done | %s: %d Wins | Draws: %d | %s: %d Wins%n",
                              game, gamesToPlay, initialBlackBot.name(), previousVersionBotWins, draws,
                              initialWhiteBot.name(), currentVersionBotWins);
        }

        long endTime = System.nanoTime();
        System.out.printf("%nTime taken: %.3f seconds", (endTime - startTime) / 1_000_000_000.0);
    }
}
