package com.chessbot.engine.utils;

import com.chessbot.application.ApplicationConfig;
import com.chessbot.engine.ai.Chessbot;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;
import com.chessbot.engine.movegen.utils.Checks;

// Runs a certain amount of games between 2 versions of the engine in order to check if a change in the evaluation/search
// function had positive or negative impact. SPRT is utilized for this
public class MatchRunner {
    static void main() {
        Sprt sprt = new Sprt(ApplicationConfig.ELO_0, ApplicationConfig.ELO_1, ApplicationConfig.ALPHA, ApplicationConfig.BETA);
        MoveList moveList = new MoveList();
        Chessbot previousVersionBot = ApplicationConfig.PREVIOUS_VERSION_BOT;
        Chessbot newVersionBot = ApplicationConfig.NEW_VERSION_BOT;

        System.out.printf("Starting SPRT Match | %s vs %s%n%n", previousVersionBot.name(), newVersionBot.name());

        // Runs a match until SPRT has enough information to decide if the change was positive/negative
        long startTime = System.nanoTime();
        int game = 0;
        while (sprt.getStatus().equals("CONTINUE")) {
            Board board = new Board();
            board.loadInitialPosition(ApplicationConfig.MATCH_RUNNER_FEN);

            // Swaps the bot's colors each game
            boolean isEvenGame = (game % 2 == 0);
            Chessbot whiteBot = isEvenGame ? newVersionBot : previousVersionBot;
            Chessbot blackBot = isEvenGame ? previousVersionBot : newVersionBot;

            // Runs a game
            double testBotScore;
            while (true) {
                MoveGenerator.generate(board, moveList);
                boolean inCheck = Checks.calculateSquares(board, board.getTurn()) != 0L;

                if (ResultDetector.isCheckmate(moveList, inCheck)) {
                    // Because the turn changes after every move, if there is checkmate, and it's black's turn, it means
                    // that white is the one that delivered the checkmate
                    boolean whiteWon = (board.getTurn() == Piece.BLACK);
                    testBotScore = (whiteWon == isEvenGame) ? 1.0 : 0.0;
                    break;
                }

                if (ResultDetector.isStalemate(moveList, inCheck) || ResultDetector.isFiftyMoveRule(board) ||
                    ResultDetector.isInsufficientMaterial(board) || ResultDetector.isThreefoldRepetition(board)
                ) {
                    testBotScore = 0.5;
                    break;
                }

                Chessbot activeBot = (board.getTurn() == Piece.WHITE) ? whiteBot : blackBot;
                board.makeMove(activeBot.chooseMove(board));
            }

            sprt.addResult(testBotScore);
            game += 1;

            System.out.printf("Game %d | %s: %d | Draws: %d | %s: %d | Progress: %.2f%%%n",
                              game, previousVersionBot.name(), sprt.getLosses(), sprt.getDraws(), newVersionBot.name(),
                              sprt.getWins(), sprt.calculateProgressPercentage()
            );
        }

        long endTime = System.nanoTime();
        System.out.printf("%nMatch Finished | Result: %s | Time Taken: %.2f seconds", sprt.getStatus(), (endTime - startTime) / 1_000_000_000.0);
    }
}
