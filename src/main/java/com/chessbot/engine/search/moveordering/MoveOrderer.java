package com.chessbot.engine.search.moveordering;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.movegen.MoveList;

public final class MoveOrderer {
    private MoveOrderer() {}


    // Goes through every legal move and adds all the different move ordering heuristic scores to the last 16 bits of the Move int
    public static void prepareMoveScores(Board board, MoveList moveList) {
        for (int i = 0; i < moveList.count; i += 1) {
            int move = moveList.moves[i];
            int score = MvvLva.scoreMove(board, move);
            score += Promotions.scoreMove(move);

            moveList.moves[i] = move | (score << 16);
        }
    }


    // Starting from the currently searched index of the given move list, it finds the best move based on the scores assigned from
    // the move ordering heuristics. Then the best move is placed at the currently searched index
    public static void selectNextMove(MoveList moveList, int currentIndex) {
        int maxMove = moveList.moves[currentIndex];
        int maxIndex = currentIndex;

        // Because the score is in the upper bits of the Move int, it doesn't have to be extracted, the whole object can just
        // be compared
        for (int i = currentIndex + 1; i < moveList.count; i += 1) {
            if (moveList.moves[i] > maxMove) {
                maxMove = moveList.moves[i];
                maxIndex = i;
            }
        }

        if (maxIndex != currentIndex) {
            int temp = moveList.moves[currentIndex];
            moveList.moves[currentIndex] = moveList.moves[maxIndex];
            moveList.moves[maxIndex] = temp;
        }
    }
}
