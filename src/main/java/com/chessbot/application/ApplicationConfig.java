package com.chessbot.application;

import com.chessbot.engine.ai.Chessbot;
import com.chessbot.engine.ai.NewVersionBot;
import com.chessbot.engine.ai.PreviousVersionBot;

public final class ApplicationConfig {
    private ApplicationConfig() {}


    public static final int WHITE_PLAYER_TYPE = PlayerType.HUMAN;
    public static final int BLACK_PLAYER_TYPE = PlayerType.HUMAN;

    // If white or black is an engine player type, they will use these versions of the engine. They are named initial because
    // they can swap sides after each game
    public static final Chessbot PREVIOUS_VERSION_BOT = new PreviousVersionBot("v6 - Tempo Bonus");
    public static final Chessbot NEW_VERSION_BOT = new NewVersionBot("v6.1 - Game Phases Tempo Bonus");

    // The search depth that the different bot versions target. The search can exceed this number
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
