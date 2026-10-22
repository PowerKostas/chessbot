package com.chessbot.engine.search;

import com.chessbot.application.ApplicationConfig;
import com.chessbot.engine.core.Constants;

import java.util.Arrays;

public final class TranspositionTable {
    public static final byte EXACT_BOUND = 1; // alpha < evaluation < beta
    public static final byte LOWER_BOUND = 2; // Fail-High node (evaluation >= beta)
    public static final byte UPPER_BOUND = 3; // Fail-Low node (evaluation <= alpha)

    // UNUSABLE_EVALUATION means that the position was found in the table, but its stored evaluation cannot be used to avoid
    // searching the current transposition again. NO_TT_ENTRY means that the position isn't in the transposition table
    public static final int UNUSABLE_EVALUATION = Integer.MIN_VALUE;
    public static final long NO_TT_ENTRY = 0L;

    // The transposition table array stores each entry across two consecutive long slots. Even indices hold the Zobrist
    // key of the position, odd indices hold the long object described below
    private final long[] transpositionTable = new long[ApplicationConfig.TT_SIZE * 2];

    /*
    The long object that occupies the odd entries of the transposition table:

    Bits 0-15 = Best move of the position without its move ordering score. The score is in the upper 16 bits of a Move
    int

    Bits 16-23 = Depth to which this position was searched, holds values up to 256. Stockfish has a maximum search depth
    of 245 plies, I usually have 128

    Bits 24-25 = Bound type (exact, lower, upper)

    Bits 26-31 = Unused

    Bits 32-63 = Signed evaluation, holds values from -2^31 to 2^31
    */
    private static final long BEST_MOVE_MASK = 0b1111111111111111L;
    private static final long DEPTH_MASK = 0b11111111L << 16;
    private static final long BOUND_TYPE_MASK = 0b11L << 24;
    private static final long EVALUATION_MASK = 0xFFFFFFFFL << 32;


    private static long packEntryData(int bestMove, int depth, byte boundType, int evaluation) {
        return bestMove | ((long) depth << 16) | ((long) boundType << 24) | ((long) evaluation << 32);
    }


    public static int unpackBestMove(long entryData) { return (int) (entryData & BEST_MOVE_MASK); }

    private static int unpackDepth(long entryData) { return (int) ((entryData & DEPTH_MASK) >>> 16); }

    private static int unpackBoundType(long entryData) { return (int) ((entryData & BOUND_TYPE_MASK) >>> 24); }

    private static int unpackEvaluation(long entryData) { return (int) ((entryData & EVALUATION_MASK) >> 32); }


    public void clear() {
        Arrays.fill(transpositionTable, 0L);
    }


    // Calculates the even TT index for the given Zobrist key
    private static int calculateEntryZobristKeyIndex(long zobristKey) {
        return ((int) zobristKey & (ApplicationConfig.TT_SIZE - 1)) << 1;
    }


    public void store(long zobristKey, int pliesFromRoot, int bestMove, int depth, byte boundType, int evaluation) {
        int entryZobristKeyIndex = calculateEntryZobristKeyIndex(zobristKey);

        // If this position is a transposition
        if (zobristKey == transpositionTable[entryZobristKeyIndex]) {
            long oldEntryData = transpositionTable[entryZobristKeyIndex + 1];

            // If a new best move wasn't found (fail-low node for example), keep the previously stored best move
            if (bestMove == 0) {
                bestMove = unpackBestMove(oldEntryData);
            }

            // Prevents overwriting a deeper search with a shallower one
            if (unpackDepth(oldEntryData) > depth) {
                return;
            }
        }

        // Cancels the root distance in order to store an independent checkmate evaluation
        if (evaluation > Constants.CHECKMATE_EVALUATION_THRESHOLD) {
            evaluation += pliesFromRoot;
        }

        else if (evaluation < -Constants.CHECKMATE_EVALUATION_THRESHOLD) {
            evaluation -= pliesFromRoot;
        }

        transpositionTable[entryZobristKeyIndex] = zobristKey;
        transpositionTable[entryZobristKeyIndex + 1] = packEntryData(bestMove, depth, boundType, evaluation);
    }


    // The entry data of the transposition is returned
    public long probeEntryData(long zobristKey) {
        int entryZobristKeyIndex = calculateEntryZobristKeyIndex(zobristKey);

        // Checks if the given Zobrist key doesn't match any entry. This happens either because this position isn't a transposition
        // or because this position was a transposition, but it got overwritten. Overwrites will happen because the possible
        // Zobrist keys are 2^64 which will always be a higher number than the TT size
        return (zobristKey == transpositionTable[entryZobristKeyIndex]) ? transpositionTable[entryZobristKeyIndex + 1] : NO_TT_ENTRY;
    }


    // An unusable/usable evaluation of the position is returned
    public static int resolveEvaluation(long entryData, int pliesFromRoot, int depth, int alpha, int beta) {
        // An unusable evaluation is returned if the stored evaluation's search depth is lower than the current search depth
        // because it means that this transposition can now be searched deeper
        if (unpackDepth(entryData) < depth) {
            return UNUSABLE_EVALUATION;
        }

        int evaluation = unpackEvaluation(entryData);

        // Reinjects the root distance into the evaluation
        if (evaluation > Constants.CHECKMATE_EVALUATION_THRESHOLD) {
            evaluation -= pliesFromRoot;
        }

        else if (evaluation < -Constants.CHECKMATE_EVALUATION_THRESHOLD) {
            evaluation += pliesFromRoot;
        }

        // Returns a usable evaluation if the transposition has an exact bound, as that represents the true value. A usable
        // evaluation is also returned if the transposition has a partial bound. In that case just checking the bound type
        // is insufficient because knowing that an evaluation is a floor or a ceiling doesn't guarantee that it will exceed
        // the current search limits
        int boundType = unpackBoundType(entryData);
        if (boundType == EXACT_BOUND) {
            return evaluation;
        }

        if (boundType == LOWER_BOUND && evaluation >= beta) {
            return evaluation;
        }

        if (boundType == UPPER_BOUND && evaluation <= alpha) {
            return evaluation;
        }

        return UNUSABLE_EVALUATION;
    }
}
