package com.chessbot.application;

import com.chessbot.engine.ai.Chessbot;
import com.chessbot.engine.ai.NewVersionBot;
import com.chessbot.engine.ai.PreviousVersionBot;

public final class ApplicationConfig {
    private ApplicationConfig() {}


    public static final int WHITE_PLAYER_TYPE = PlayerType.HUMAN;
    public static final int BLACK_PLAYER_TYPE = PlayerType.ENGINE;

    // Configures the versions of the engine when it's a human vs engine game and when it's an engine vs engine game
    public static final Chessbot PREVIOUS_VERSION_BOT = new PreviousVersionBot("v6.1 - Game Phases Tempo Bonus");
    public static final Chessbot NEW_VERSION_BOT = new NewVersionBot("v7 - PSTs");

    public static final Chessbot HUMAN_OPPONENT_BOT = NEW_VERSION_BOT;
    public static final Chessbot ENGINE_MATCH_WHITE_BOT = NEW_VERSION_BOT;
    public static final Chessbot ENGINE_MATCH_BLACK_BOT = PREVIOUS_VERSION_BOT;

    // The search depth that the different engine versions target. The search can exceed this number
    public static final int AI_TARGET_SEARCH_DEPTH = 4;

    // SPRT parameters
    public static final double ELO_0 = 0;
    public static final double ELO_1 = 10;
    public static final double ALPHA = 0.05;
    public static final double BETA = 0.05;

    public static final int PERFT_SEARCH_DEPTH = 6;

    public static final String UI_GAME_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static final String ENDLESS_MODE_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static final String MATCH_RUNNER_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static final String PERFT_FEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
}
