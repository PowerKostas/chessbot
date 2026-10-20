package com.chessbot.engine.ai;

import com.chessbot.application.ApplicationConfig;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.search.Searcher;
import com.chessbot.engine.search.TranspositionTable;

public final class NewVersionBot implements Chessbot {
    private final String name;
    private final TranspositionTable tt = new TranspositionTable();

    public NewVersionBot(String name) { this.name = name; }


    @Override
    public String name() { return name; }

    @Override
    public int chooseMove(Board board) {
        Searcher searcher = new Searcher(board, tt, true);
        return searcher.searchRoot(ApplicationConfig.AI_TARGET_SEARCH_DEPTH);
    }

    @Override
    public void prepareNewGame() { tt.clear(); }
}
