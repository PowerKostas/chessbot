package com.chessbot.engine.search;

import com.chessbot.application.ApplicationConfig;
import com.chessbot.engine.core.Constants;
import java.util.Arrays;

public final class TranspositionTable {
    public static final int NO_SCORE = Integer.MIN_VALUE;
    public static final byte EXACT_BOUND = 1; // alpha < score < beta
    public static final byte LOWER_BOUND = 2; // Fail-High node (score >= beta)
    public static final byte UPPER_BOUND = 3; // Fail-Low node (score <= alpha)

    private final long[] keys = new long[ApplicationConfig.TT_SIZE];
    private final int[] moves = new int[ApplicationConfig.TT_SIZE];
    private final int[] evaluations = new int[ApplicationConfig.TT_SIZE];
    private final byte[] depths = new byte[ApplicationConfig.TT_SIZE];
    private final byte[] bounds = new byte[ApplicationConfig.TT_SIZE];


    // If the keys are cleared, any future lookup will fail the check and the stale data will eventually be overwritten
    public void clear() {
        Arrays.fill(keys, 0L);
    }


    // Gets a random index up to the TT size based on the Zobrist key
    private int getIndex(long zobristKey) {
        return (int) zobristKey & (ApplicationConfig.TT_SIZE - 1);
    }


    public void store(long zobristKey, int pliesFromRoot, int evaluation, int bestMove, int depth, byte bound) {
        int index = getIndex(zobristKey);

        // If this position is a transposition
        if (zobristKey == keys[index]) {
            // If a new best move wasn't found (fail-low node for example), keep the previously stored best move
            if (bestMove == 0) {
                bestMove = moves[index];
            }

            // Prevents overwriting a deeper search with a shallower one
            if (depths[index] > depth) {
                return;
            }
        }

        // Cancels the root distance in order to store an independent checkmate evaluation
        if (evaluation > Constants.CHECKMATE_SCORE_THRESHOLD) {
            evaluation += pliesFromRoot;
        }

        else if (evaluation < -Constants.CHECKMATE_SCORE_THRESHOLD) {
            evaluation -= pliesFromRoot;
        }

        keys[index] = zobristKey;
        evaluations[index] = evaluation;
        moves[index] = bestMove;
        depths[index] = (byte) depth;
        bounds[index] = bound;
    }


    // Core function of the TT. The goal is to see if a stored evaluation can be used in order to avoid searching the current
    // transposition again
    public int probeScore(long zobristKey, int depth, int pliesFromRoot, int alpha, int beta) {
        int index = getIndex(zobristKey);

        // Returns an unusable evaluation if the given Zobrist key doesn't match any entry. This happens either because
        // this position isn't a transposition or because this position was a transposition, but it got overwritten. Overwrites
        // will happen because the possible Zobrist keys are 2^64 which will always be a higher number than the TT size.
        // An unusable evaluation is also returned if the stored evaluation's search depth is lower than the current search
        // depth because it means that this transposition can now be searched deeper
        if (zobristKey != keys[index] || depths[index] < depth) {
            return NO_SCORE;
        }

        int evaluation = evaluations[index];

        // Reinjects the root distance into the evaluation
        if (evaluation > Constants.CHECKMATE_SCORE_THRESHOLD) {
            evaluation -= pliesFromRoot;
        }

        else if (evaluation < -Constants.CHECKMATE_SCORE_THRESHOLD) {
            evaluation += pliesFromRoot;
        }

        // Returns a usable evaluation if the transposition has an exact bound, as that represents the true value. A usable
        // evaluation is also returned if the transposition has a partial bound. In that case just checking the bound type
        // is insufficient because knowing that an evaluation is a floor or a ceiling doesn't guarantee that it will exceed
        // the current search limits
        byte bound = bounds[index];
        if (bound == EXACT_BOUND) {
            return evaluation;
        }

        if (bound == LOWER_BOUND && evaluation >= beta) {
            return evaluation;
        }

        if (bound == UPPER_BOUND && evaluation <= alpha) {
            return evaluation;
        }

        return NO_SCORE;
    }


    // Even when a TT entry isn't searched deep enough to immediately cause a cut-off, its stored bestMove is almost always
    // the strongest move to try first. This function is used in move ordering
    public int probeBestMove(long zobristKey) {
        int index = getIndex(zobristKey);
        return (zobristKey == keys[index]) ? moves[index] : 0;
    }
}
