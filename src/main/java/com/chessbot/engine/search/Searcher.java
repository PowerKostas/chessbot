package com.chessbot.engine.search;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Constants;
import com.chessbot.engine.evaluation.Evaluator;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;
import com.chessbot.engine.movegen.utils.Checks;
import java.util.Random;

// Searcher is a standard class instead of a utility one in order to be able to create multiple instances if needed
public class Searcher {
    private final Board board;

    // Array to hold the pre-allocated move list objects, one object per depth level is needed
    private final MoveList[] moveListPool = new MoveList[Constants.MAX_SEARCH_DEPTH];

    // Boolean to apply experimental features only to specific bot versions
    private final boolean experimentalVersion;


    public Searcher(Board board, boolean experimentalVersion) {
        this.board = board;

        for (int i = 0; i < Constants.MAX_SEARCH_DEPTH; i += 1) {
            this.moveListPool[i] = new MoveList();
        }

        this.experimentalVersion = experimentalVersion;
    }


    /*
    SEARCH ARCHITECTURE:

    1. Negamax: The core tree search algorithm, the philosophy is to translate everything into the current player's perspective. It
       traverses the possible moves depth-first, assigning a score to each leaf node based on the evaluation function. Each parent
       node takes the maximum of the negated values returned by its children. This allows it to pick the move that results in the
       worst possible position for the current opponent. In the example diagram, every node is a position's evaluation score from the
       perspective of the player to the right and every line is a move that caused the position

                   2               | Black
           /       |       \
          6        9       -2      | White
        / | \    / | \    / | \
       4 -6  0  -8 7 -9  4  2  6   | Black

    2. Alpha-Beta Pruning: In a Negamax context, alpha represents the best evaluation score the current player has already
       guaranteed elsewhere in the search. Beta represents the best evaluation score the current opponent has already guaranteed
       elsewhere in the search. In other words, for every child score of the current player, alpha is the floor and beta is
       the ceiling. If a score is below the floor, it's ignored, if it's at or above the ceiling, the whole branch is pruned
       because the opponent would never choose to go down this path. Alpha and beta values are swapped and negated at every depth
       to accurately portray the shifting perspectives. In the Negamax example diagram, when the 0 node is reached, alpha = 6 and
       beta = inf for white. When the -8 node is reached, alpha = 8 and beta = 6 for white, for that reason the 9, -8, 7, -9
       branch is pruned
    */

    // Entry function for the Searcher class. Called once per turn, responsible for searching the root and returning the chosen
    // move. It's separated from search() because only this function needs to return a move
    public int searchRoot(int depth) {
        MoveList moveList = moveListPool[0];
        MoveGenerator.generate(board, moveList);

        Random random = new Random();
        int ties = 0;

        int bestEvaluation = -Constants.INFINITY_SCORE;
        int bestMove = moveList.moves[0];
        for (int i = 0; i < moveList.count; i += 1) {
            int move = moveList.moves[i];
            int undo = board.makeMove(move);
            int evaluation = -search(depth - 1, 1, -Constants.INFINITY_SCORE, Constants.INFINITY_SCORE);
            board.unmakeMove(move, undo);

            if (evaluation > bestEvaluation) {
                bestEvaluation = evaluation;
                bestMove = move;
                ties += 1;
            }

            else if (evaluation == bestEvaluation) {
                ties += 1;

                if (random.nextInt(ties) == 0) {
                    bestMove = move;
                }
            }
        }

        return bestMove;
    }

     // Recursive function for every depth below the root. Returns an evaluation score. The pliesFromRoot variable counts
     // the number of half moves from the root position
    private int search(int depth, int pliesFromRoot, int alpha, int beta) {
        if (depth == 0) {
            return Evaluator.evaluate(board, experimentalVersion);
        }

        MoveList moveList = moveListPool[pliesFromRoot];
        MoveGenerator.generate(board, moveList);

        // If this move results in checkmate, return a terrible evaluation score adjusted by the plies from root number. This
        // ensures that the engine favors the current player getting mated in 5 over getting mated in 1. If this move results
        // in a draw, return an evaluation score of 0
        if (moveList.count == 0) {
            boolean inCheck = Checks.calculateSquares(board, board.getTurn()) != 0L;
            return inCheck ? -Constants.CHECKMATE_SCORE + pliesFromRoot : 0;
        }

        for (int i = 0; i < moveList.count; i += 1) {
            int move = moveList.moves[i];
            int undo = board.makeMove(move);
            int evaluation = -search(depth - 1, pliesFromRoot + 1, -beta, -alpha);
            board.unmakeMove(move, undo);

            if (evaluation >= beta) {
                return beta;
            }

            alpha = Math.max(alpha, evaluation);
        }

        return alpha;
    }
}
