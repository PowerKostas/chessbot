package com.chessbot.engine.tuning;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Constants;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.evaluation.Evaluator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// Because this class holds millions of Board objects, the program runs out of memory. In Run Configurations > VM options,
// use the command -Xmx8G/-Xmx16G/... to allocate more memory. The reason for the array of Boards is so I can use the already
// made evaluation function, this avoids having to maintain 2 separate evaluation functions or Board objects. The optimization
// method can take multiple hours to complete and the calculateInitialScores() and syncDerivedEvalParams() functions are
// wastefully called on every parameter change. Because I will only need to tune the evaluation function parameters like
// 3 times, it's okay
public final class TexelTuner {
    private static Board[] boards;
    private static double[] results; // 0.0 = Black win, 0.5 Draw, 1.0 White win
    private static final double SIGMOID_SCALE = Math.log(10.0) / 400.0;

    private TexelTuner() {}


    // Extracts the fens/results from the dataset. For speed purposes, boards objects are preloaded using each FEN
    private static void loadDataset() {
        List<Board> tempBoards = new ArrayList<>();
        List<Double> tempResults = new ArrayList<>();

        try (Stream<String> lines = Files.lines(Path.of("src/main/resources/com/chessbot/datasets/E12.52-1M-D12-Resolved.book"))) {
            lines.forEach(line -> {
                // Splits by either '[' or ']'
                String[] parts = line.split("[\\[\\]]");

                Board board = new Board(0);
                board.loadInitialPosition(parts[0].strip());
                tempBoards.add(board);
                tempResults.add(Double.parseDouble(parts[1]));
            });
        }

        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        boards = tempBoards.toArray(new Board[0]);
        results = tempResults.stream().mapToDouble(Double::doubleValue).toArray();
    }


    // Returns the evaluation of the given FEN's position, it's positive/negative when white/black is winning
    private static int evaluate(Board board) {
        int evaluation = Evaluator.evaluate(board, true);
        return (board.getTurn() == Piece.WHITE) ? evaluation : -evaluation;
    }


    // The elo formula is used for the sigmoid function
    private static double sigmoid(int evaluation) {
        return 1.0 / (1.0 + Math.exp(-evaluation * SIGMOID_SCALE));
    }


    // Based on every FEN in the dataset, compute the Mean Squared Error between the engine's evaluations and the actual
    // results. Because they are 2 different metrics, a sigmoid function is used to turn the evaluations to win probabilities
    // from the perspective of white. The loop is done in parallel for speed. Additionally, scores are recalculated here
    // because every time a mg/eg/phase parameter changes, the scores are out of sync
    private static double computeMSE() {
        return IntStream.range(0, boards.length)
            .parallel()
            .mapToDouble(i -> {
                Board board = boards[i];
                board.calculateInitialScores();
                double diff = results[i] - sigmoid(evaluate(board));
                return diff * diff;
            })
            .average()
            .orElse(0.0);
    }


    // Local search, optimizes the evaluation function parameters by testing incremental changes repeatedly. If MSE improves
    // when a step size is added/subtracted to a parameter, the change is kept and another pass over all the parameters
    // is needed. This goes on until MSE can't improve anymore even with the smallest step size
    static void main() {
        loadDataset();
        System.out.printf("Loaded %d positions%n%n", boards.length);

        // Dynamic step sizing (steps are centipawn increments). It executes bigger leaps first and when a pass yields zero
        // improvements to MSE, it narrows down
        double bestError = computeMSE();
        for (int step = 8; step >= 1; step /= 2) {
            boolean improved = true;
            int pass = 0;

            while (improved) {
                improved = false;

                pass += 1;
                System.out.printf("Step: %d | Pass: %d | MSE: %f%n", step, pass, bestError);

                for (int i = 0; i < Constants.EVAL_PARAMS_COUNT; i += 1) {
                    // Skips the parameter if it shouldn't be tuned
                    if (Arrays.binarySearch(Constants.UNTUNABLE_EVAL_PARAM_INDICES, i) >= 0) {
                        continue;
                    }

                    int originalEvalParam = Constants.EVAL_PARAMS[i];

                    Constants.EVAL_PARAMS[i] = originalEvalParam + step;
                    Constants.syncDerivedEvalParams();
                    double errorUp = computeMSE();
                    if (errorUp < bestError) {
                        bestError = errorUp;
                        improved = true;
                        continue;
                    }

                    Constants.EVAL_PARAMS[i] = originalEvalParam - step;
                    Constants.syncDerivedEvalParams();
                    double errorDown = computeMSE();
                    if (errorDown < bestError) {
                        bestError = errorDown;
                        improved = true;
                        continue;
                    }

                    // If neither direction helped, revert back to the original state
                    Constants.EVAL_PARAMS[i] = originalEvalParam;
                    Constants.syncDerivedEvalParams();
                }
            }
        }

        // Prints the optimized evaluation function parameters so I can write them down
        for (int i = 0; i < Constants.EVAL_PARAMS_COUNT; i += 1) {
            System.out.printf("%nEVAL_PARAMS[%d] = %d", i, Constants.EVAL_PARAMS[i]);
        }
    }
}
