package com.chessbot.engine.search;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Constants;
import com.chessbot.engine.core.Move;
import com.chessbot.engine.core.Piece;
import com.chessbot.engine.evaluation.Evaluator;
import com.chessbot.engine.evaluation.Material;
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

    1. Negamax: The core tree search algorithm. It's based on the Minimax algorithm which determines a score after a certain
       number of moves. It assumes best play according to the evaluation function. The difference of Negamax is that it
       translates everything into the current player's perspective instead of having to handle 2 players. It traverses the
       possible moves depth-first, assigning a score to each leaf node based on the evaluation function. Each parent node
       takes the maximum of the negated values returned by its children. This allows it to pick the move that results in
       the worst possible position for the current opponent. In the example diagram, every node is a position's evaluation
       score from the perspective of the player to the right and every line is a move that caused the position

                   2               | Black
           /       |       \
          6        9       -2      | White
        / | \    / | \    / | \
       4 -6  0  -8 7 -9  4  2  6   | Black

    2. Alpha-Beta Pruning: In a Negamax context, alpha represents the best evaluation the current player has already guaranteed
       elsewhere in the search. Beta represents the best evaluation the current opponent has already guaranteed elsewhere
       in the search. In other words, for every child score of the current player, alpha is the floor and beta is the ceiling.
       If a score is below the floor, it's ignored, if it's at or above the ceiling, the branch is pruned because the opponent
       would never choose to go down this path. Alpha and beta values are swapped and negated at every depth to accurately
       portray the shifting perspectives. In the Negamax example diagram, when the 0 node is reached, alpha = 6 and beta
       = inf for white. When the -8 node is reached, alpha = -inf and beta = 6 for white, that's why the remaining 7 and
       -9 nodes will be pruned. A fail-soft implementation is used. If a move scores 300 but beta is 100, the search will
       return 300 to the parent node. The returned score should mean one of three things:

       1. In a Fail-Low node (score <= alpha): An upper bound value should be returned because the position is worth at
       most this much. No move that the current player tried was better than alpha, so the true value can't be higher than
       the score

       2. In a Fail-High node (score >= beta): A lower bound value should be returned because the position is worth at least
       this much. A move was good enough to cause a beta-cutoff and the remaining moves were never looked at, so the true
       value could be even higher

       3. In between alpha and beta scenarios: The score is exact

       This extra data will be useful later on

    3. Move Ordering: If the strong moves are evaluated first, massive branches of the search tree can be pruned thanks
       to the way Alpha-Beta Pruning works. A score based on different heuristics is incorporated in the Move int. The node
       with the biggest score is searched first every time. The heuristics used are: MVV-LVA and Promotions, more information
       on the respective classes

    4. Quiescence Search: Performs a more limited search at the end of the normal search. Without it the engine blunders
       all the time. The reason for that is that it doesn't know if the opponent can just capture its queen one move after
       the searched depth, for example. If not in check, the limited search only includes captures and promotions, otherwise
       all legal moves have to be searched in order to find check evasions. QS terminates when a quiet position is reached
       (no captures, promotions or check evasions left), when a beta-cutoff occurs or when the maximum search depth is reached
    */

    // Entry function for the Searcher class. Called once per turn, responsible for searching the root and returning the
    // chosen move. It's separated from search() because only this function needs to return a move. If there are 0 legal
    // moves in this position, moveList.moves[0] would return a null, if it's the first move of the game, or a random illegal
    // move, anywhere else. But this isn't a problem because the end of the game is already handled by whichever class originally
    // called this function
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

            alpha = bestEvaluation - 1;
        }

        //System.out.printf("Nodes Searched: %d%n", nodesSearched);

        return bestMove;
    }

     // Recursive function for every depth below the root, returns an evaluation
    private int search(int depth, int pliesFromRoot, int alpha, int beta) {
        //nodesSearched += 1;

        if (depth == 0) {
            return quiescenceSearch(pliesFromRoot, alpha, beta);
        }

        MoveList moveList = moveListPool[pliesFromRoot];
        MoveGenerator.generate(board, moveList);

        // If this move results in checkmate, return a terrible evaluation. The terrible evaluation is adjusted by the plies
        // from root number, this ensures that the engine favors the current player getting mated in 5 over getting mated
        // in 1. If this move results in a draw, return an evaluation of 0
        if (moveList.count == 0) {
            boolean inCheck = Checks.calculateSquares(board, board.getTurn()) != 0L;
            return inCheck ? -Constants.CHECKMATE_SCORE + pliesFromRoot : 0;
        }

        MoveOrderer.prepareMoveScores(board, moveList);

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

    // Since quiescence search has no depth limit, only pliesFromRoot is needed
    private int quiescenceSearch(int pliesFromRoot, int alpha, int beta) {
        //nodesSearched += 1;

        // Initializes a stand-pat value. Stand-pat is the evaluation of the board after the opponent has made their move
        // and before the current player makes theirs. In other words the baseline score
        int standPat = Evaluator.evaluate(board, experimentalVersion);

        // If in check using stand-pat is not allowed. There are 2 reasons for that. Firstly, we are not sure that there
        // is a move that can match alpha, in many positions a check can mean a serious threat that cannot be resolved.
        // Secondly, stand-pat assumes that even if we finish searching all moves and none of them increase alpha, one
        // of the quiet moves can most likely increase alpha. But because instead of a limited square, a full search is
        // executed when in check, the above can't be valid
        boolean inCheck = Checks.calculateSquares(board, board.getTurn()) != 0L;
        if (!inCheck) {
            // If the baseline score is already so high, the opponent would never choose to go down this path, for that
            // reason the branch is just pruned
            if (standPat >= beta) {
                return standPat;
            }

            // Checks if ANY move can improve alpha. Big delta represents the best theoretical move which is a max value
            // queen capture plus a safety margin (200 centipawns is standard) plus a potential queen promotion. If not
            // even that move can save the position, it's a truly hopeless node, and it's pruned early. In MoveOrdering.MVV-LVA,
            // a queen capture with a pawn gets a score of 40, in MoveOrdering.Promotions, a queen promotion gets a score
            // of 35. That scale is used to calculate the increment to big delta when a pawn can promote. This method doesn't
            // seem to have a noticeable impact on performance, but it doesn't hurt it either
            int BIG_DELTA = Material.getMaxPieceValue(Piece.QUEEN) + 200;

            boolean canPromote;
            if (board.getTurn() == Piece.WHITE) {
                // White pawn on the 7th rank
                canPromote = (board.getBitboard(Piece.WHITE, Piece.PAWN) & 0x00FF000000000000L) != 0L;
            }

            else {
                // Black pawn on the 2nd rank
                canPromote = (board.getBitboard(Piece.BLACK, Piece.PAWN) & 0x000000000000FF00L) != 0L;
            }

            if (canPromote) {
                BIG_DELTA += (Material.getMaxPieceValue(Piece.QUEEN) * 35) / 40;
            }

            if (standPat + BIG_DELTA <= alpha) {
                return standPat + BIG_DELTA;
            }

            // Updates alpha because the baseline might be better than the current guaranteed alpha
            if (standPat > alpha) {
                alpha = standPat;
            }
        }

        // Because a quiescence search can theoretically follow a long chain of moves, return the static evaluation if the
        // search exceeds the given max search depth
        if (pliesFromRoot >= Constants.MAX_SEARCH_DEPTH) {
            return standPat;
        }

        MoveList moveList = moveListPool[pliesFromRoot];
        MoveGenerator.generate(board, moveList);

        if (moveList.count == 0) {
            return inCheck ? -Constants.CHECKMATE_SCORE + pliesFromRoot : 0;
        }

        MoveOrderer.prepareMoveScores(board, moveList);

        // If in check, the floor equals negative infinity because any legal move is better than being in check. Otherwise,
        // the floor equals just the stand-pat score
        int bestEvaluation = inCheck ? -Constants.INFINITY_SCORE : standPat;

        for (int i = 0; i < moveList.count; i += 1) {
            MoveOrderer.selectNextMove(moveList, i);
            int move = moveList.moves[i];
            int flag = Move.getFlag(move);

            if (!inCheck) {
                // Only queen promotions are worth looking at a quiescence search
                boolean isCaptureOrPromotion = flag == Move.FLAG_CAPTURE ||
                                               flag == Move.FLAG_EN_PASSANT_CAPTURE ||
                                               flag == Move.FLAG_QUEEN_PROMOTION ||
                                               flag == Move.FLAG_QUEEN_PROMOTION_CAPTURE;

                if (!isCaptureOrPromotion) {
                    continue;
                }

                // Delta Pruning. Before a capture is made, test if the captured piece value plus a safety margin (200 centipawns
                // is standard) are enough to raise alpha. If they are not, the capture is unlikely to improve the position
                // and it's pruned. Should be switched off in the late endgame, since otherwise the search would be blind
                // to insufficient material issues and transitions into won endgames made at the expense of some material.
                // Delta Pruning is technically a Fail-Low filter because it prunes captures if they are less than or equal
                // to alpha. For that reason, the score uses the upper bound/maximum piece value
                if (flag == Move.FLAG_CAPTURE) {
                    int capturedPieceValue = Material.getMaxPieceValue(board.getPieceTypeAtSquare(Move.getEndingSquare(move)));
                    if (standPat + capturedPieceValue + 200 <= alpha) {
                        continue;
                    }
                }

                else if (flag == Move.FLAG_EN_PASSANT_CAPTURE) {
                    if (standPat + 300 <= alpha) {
                        continue;
                    }
                }
            }

            int undo = board.makeMove(move);
            int evaluation = -quiescenceSearch(pliesFromRoot + 1, -beta, -alpha);
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
