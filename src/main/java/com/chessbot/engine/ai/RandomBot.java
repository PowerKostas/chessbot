package com.chessbot.engine.ai;

import com.chessbot.engine.core.Board;
import com.chessbot.engine.movegen.MoveGenerator;
import com.chessbot.engine.movegen.MoveList;
import java.util.Random;

public class RandomBot implements Chessbot {
    private final MoveList moveList = new MoveList();
    private final Random random = new Random();


    @Override
    public String name() {
        return "v0.1 - Random Moves";
    }


    @Override
    public int chooseMove(Board board) {
        MoveGenerator.generate(board, moveList);
        return moveList.moves[random.nextInt(moveList.count)];
    }
}
