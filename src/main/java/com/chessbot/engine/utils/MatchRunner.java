package com.chessbot.engine.utils;

import com.chessbot.application.ApplicationConfig;
import com.chessbot.engine.ai.Chessbot;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;
import com.chessbot.engine.movegen.utils.Checks;

import java.util.Random;

// Runs a certain amount of games between 2 versions of the engine in order to check if a change in the evaluation/search
// function had positive or negative impact. SPRT is utilized for this
public class MatchRunner {
    static void main() {
        Sprt sprt = new Sprt(ApplicationConfig.ELO_0, ApplicationConfig.ELO_1, ApplicationConfig.ALPHA, ApplicationConfig.BETA);
        MoveList moveList = new MoveList();
        Chessbot previousVersionBot = ApplicationConfig.PREVIOUS_VERSION_BOT;
        Chessbot newVersionBot = ApplicationConfig.NEW_VERSION_BOT;

        System.out.printf("Starting SPRT Match | %s vs %s%n%n", previousVersionBot.name(), newVersionBot.name());

        long startTime = System.nanoTime();

        Random random = new Random(12345);
        int[] opening = null;

        int game = 0;
        while (sprt.getStatus().equals("CONTINUE")) {
            Board board = new Board();
            board.loadInitialPosition(ApplicationConfig.MATCH_RUNNER_FEN);
            previousVersionBot.prepareNewGame();
            newVersionBot.prepareNewGame();

            // Swaps the bot's colors each game
            boolean isEvenGame = (game % 2 == 0);
            Chessbot whiteBot = isEvenGame ? newVersionBot : previousVersionBot;
            Chessbot blackBot = isEvenGame ? previousVersionBot : newVersionBot;

            // A new random opening is generated every 2 games. The odd game replays the same opening with colors swapped
            if (isEvenGame) {
                opening = generateRandomOpening(random, moveList);
            }

            for (int move : opening) {
                board.makeMove(move);
            }

            // Runs a game
            double testBotScore;
            while (true) {
                MoveGenerator.generate(board, moveList);
                boolean inCheck = Checks.calculateSquares(board, board.getTurn()) != 0L;

                if (ResultDetector.isCheckmate(moveList, inCheck)) {
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



    // Plays random legal moves from the starting position. Retries if the game ends during the opening (e.g. fool's mate
// is possible in 4 plies), so the returned opening always leads to a position with legal moves
    private static int[] generateRandomOpening(Random random, MoveList moveList) {
        while (true) {
            Board board = new Board();
            board.loadInitialPosition(ApplicationConfig.MATCH_RUNNER_FEN);
            int[] opening = new int[5];

            boolean valid = true;
            for (int i = 0; i < opening.length; i += 1) {
                MoveGenerator.generate(board, moveList);
                if (moveList.count == 0) {
                    valid = false;
                    break;
                }

                int move = moveList.moves[random.nextInt(moveList.count)];
                opening[i] = move;
                board.makeMove(move);
            }

            if (!valid) {
                continue;
            }

            // The final position must still have legal moves
            MoveGenerator.generate(board, moveList);
            if (moveList.count > 0) {
                return opening;
            }
        }
    }
}
