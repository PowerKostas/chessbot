package com.chessbot.engine.core;

public final class Constants {
    private Constants() {}


    // Stockfish has a maximum search depth of 245 plies, I went with about half of that
    public static final int MAX_SEARCH_DEPTH = 128;

    // The maximum number of plies a single game can last. The longest recorded chess game in history lasted 538 plies, I went
    // with about double of that
    public static final int MAX_GAME_MOVES = 1024;
}
