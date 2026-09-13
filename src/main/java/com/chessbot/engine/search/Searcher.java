package com.chessbot.engine.search;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Constants;
import com.chessbot.engine.evaluation.Evaluator;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;
import com.chessbot.engine.movegen.utils.Checks;
import com.chessbot.engine.search.moveordering.MoveOrderer;

import java.util.Random;

// Searcher is a standard class instead of a utility one in order to be able to create multiple instances if needed
public class Searcher {
    private final Board board;

    // Array to hold the pre-allocated move list objects, one object per depth level is needed
    private final MoveList[] moveListPool = new MoveList[Constants.MAX_SEARCH_DEPTH];

    // Boolean to apply experimental features only to specific bot versions
    private final boolean experimentalVersion;

    // Only used to test performance
    //private int nodesSearched;


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
       branch is pruned. A fail-soft implementation is used. If a move scores 300 but beta is 100, the search will return 300, this
       data will be useful later on

    3. Move Ordering: If the strong moves are evaluated first, massive branches of the search tree can be pruned. A score based
       on different heuristics is incorporated in the Move int. The node with the biggest score is searched first every
       time. The heuristics used are: MVV-LVA and Promotions, more information on the respective classes
    */

    // Entry function for the Searcher class. Called once per turn, responsible for searching the root and returning the chosen
    // move. It's separated from search() because only this function needs to return a move. If there are 0 legal moves in this
    // position, moveList.moves[0] would technically return a null, if it's the first move of the game, or a random illegal
    // move, anywhere else. But the end of the game is already handled by whichever class originally called this function
    public int searchRoot(int depth) {
        //nodesSearched = 0;
        //nodesSearched += 1;

        MoveList moveList = moveListPool[0];
        MoveGenerator.generate(board, moveList);
        MoveOrderer.prepareMoveScores(board, moveList);

        Random random = new Random();
        int ties = 0;

        int alpha = -Constants.INFINITY_SCORE;
        int beta = Constants.INFINITY_SCORE;
        int bestEvaluation = -Constants.INFINITY_SCORE;
        int bestMove = moveList.moves[0];
        for (int i = 0; i < moveList.count; i += 1) {
            MoveOrderer.selectNextMove(moveList, i);

            // Prints the root moves, useful for debugging move ordering
            //System.out.printf("%s%s%n%n", CoordinatesConverter.indexToString(Move.getStartingSquare(moveList.moves[i])), CoordinatesConverter.indexToString(Move.getEndingSquare(moveList.moves[i])));

            int move = moveList.moves[i];
            int undo = board.makeMove(move);
            int evaluation = -search(depth - 1, 1, -beta, -alpha);
            board.unmakeMove(move, undo);

            if (evaluation > bestEvaluation) {
                bestEvaluation = evaluation;
                bestMove = move;
                ties = 1;
            }

            else if (evaluation == bestEvaluation) {
                ties += 1;

                if (random.nextInt(ties) == 0) {
                    bestMove = move;
                }
            }

            alpha = bestEvaluation;
        }

        //System.out.printf("Nodes Searched: %d%n", nodesSearched);

        return bestMove;
    }

     // Recursive function for every depth below the root. Returns an evaluation score. The pliesFromRoot variable counts
     // the number of half moves from the root position
    private int search(int depth, int pliesFromRoot, int alpha, int beta) {
        //nodesSearched += 1;

        if (depth == 0) {
            return Evaluator.evaluate(board, experimentalVersion);
        }

        MoveList moveList = moveListPool[pliesFromRoot];
        MoveGenerator.generate(board, moveList);
        MoveOrderer.prepareMoveScores(board, moveList);

        // If this move results in checkmate, return a terrible evaluation score adjusted by the plies from root number. This
        // ensures that the engine favors the current player getting mated in 5 over getting mated in 1. If this move results
        // in a draw, return an evaluation score of 0
        if (moveList.count == 0) {
            boolean inCheck = Checks.calculateSquares(board, board.getTurn()) != 0L;
            return inCheck ? -Constants.CHECKMATE_SCORE + pliesFromRoot : 0;
        }

        int bestEvaluation = -Constants.INFINITY_SCORE;
        for (int i = 0; i < moveList.count; i += 1) {
            MoveOrderer.selectNextMove(moveList, i);
            int move = moveList.moves[i];
            int undo = board.makeMove(move);
            int evaluation = -search(depth - 1, pliesFromRoot + 1, -beta, -alpha);
            board.unmakeMove(move, undo);

            if (evaluation > bestEvaluation) {
                bestEvaluation = evaluation;
            }

            if (bestEvaluation >= beta) {
                return bestEvaluation;
            }

            alpha = Math.max(alpha, bestEvaluation);
        }

        return bestEvaluation;
    }
}
