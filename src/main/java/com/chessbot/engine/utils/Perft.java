package com.chessbot.engine.utils;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;

public class Perft {
    private final Board board;

    // Array to hold the pre-allocated move list objects, one object per depth level (Stockfish has a maximum search depth of
    // 245 plies, I went with about half of that)
    private final MoveList[] moveListPool = new MoveList[128];


    public Perft(Board board) {
        this.board = board;

        for (int i = 0; i < 128; i++) {
            moveListPool[i] = new MoveList();
        }
    }


    // Perft debug function with bulk-counting. Walks the move generation tree, without generating moves for leaf nodes, in order
    // to count the number of nodes in a given position
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
        FenParser.loadFen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", board);

        Perft perft = new Perft(board);
        int depth = 6;

        long startTime = System.nanoTime();
        long nodesCount = perft.calculate(depth);
        long endTime = System.nanoTime();

        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        long nps = (long) (nodesCount / durationInSeconds);

        System.out.printf("Total Nodes: %,d%n", nodesCount);
        System.out.printf("Time taken:  %.3f sec%n", durationInSeconds);
        System.out.printf("Speed:       %,d nps%n", nps);
    }
}
