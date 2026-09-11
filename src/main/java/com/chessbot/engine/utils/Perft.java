package com.chessbot.engine.utils;

import com.chessbot.application.ApplicationConfig;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.core.Constants;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;

public class Perft {
    private final Board board;

    // Array to hold the pre-allocated move list objects, one object per depth level is needed
    private final MoveList[] moveListPool = new MoveList[Constants.MAX_SEARCH_DEPTH];


    public Perft(Board board) {
        this.board = board;

        for (int i = 0; i < Constants.MAX_SEARCH_DEPTH; i += 1) {
            moveListPool[i] = new MoveList();
        }
    }


    // Perft debug function with bulk-counting. Walks the move generation tree, without generating moves for leaf nodes or caring
    // about draws by the 50-move rule, insufficient material or threefold repetition. The point is to count the number of nodes
    // in a given position in order to test the speed and accuracy of the move generation function
    public long calculate(int depth) {
        long nodesCount = 0;

        MoveList moveList = moveListPool[depth];
        MoveGenerator.generate(board, moveList);

        if (depth == 1) {
            return moveList.count;
        }

        for (int i = 0; i < moveList.count; i += 1) {
            int undo = board.makeMove(moveList.moves[i]);
            nodesCount += calculate(depth - 1);
            board.unmakeMove(moveList.moves[i], undo);
        }

        return nodesCount;
    }


    // Runner function for Perft, loads a position and prints the necessary outputs
    static void main() {
        Board board = new Board();
        FenParser.loadFen(ApplicationConfig.PERFT_FEN, board);
        Perft perft = new Perft(board);
        int depth = 5;

        long startTime = System.nanoTime();
        long nodesCount = perft.calculate(depth);
        long endTime = System.nanoTime();

        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.printf("Total Nodes: %,d%n", nodesCount);
        System.out.printf("Time taken:  %.3f seconds%n", durationInSeconds);
        System.out.printf("Speed:       %,d nps%n", (long) (nodesCount / durationInSeconds));
    }
}
