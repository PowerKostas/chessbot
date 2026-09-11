package com.chessbot.engine.ai;

import com.chessbot.application.ApplicationConfig;
import com.chessbot.engine.core.Board;
import com.chessbot.engine.search.Searcher;

public record CurrentVersionBot(String name) implements Chessbot {
    @Override
    public int chooseMove(Board board) {
        Searcher searcher = new Searcher(board, true);
        return searcher.searchRoot(ApplicationConfig.TARGET_SEARCH_DEPTH);
    }
}
