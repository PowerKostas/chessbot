package com.chessbot.BitboardUtils;

import com.chessbot.Objects.Pieces.Rook;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class MagicBitboards {
    public record Key(int startingSquare, long blockerPattern) {}


    public long[] createBlockingPatternsBitboards(long attacksBitboard) {
        List<Integer> attackIndices = new ArrayList<>();

        // Gets the indices of every 1 in the attacks bitboard, eg: 1010 = {1, 3}
        while (attacksBitboard != 0L) {
            int i = Long.numberOfTrailingZeros(attacksBitboard);
            attackIndices.add(i);
            attacksBitboard ^= (1L << i);
        }

        // Number of patterns = 2 ^ number of valid attacks
        int numPatterns = 1 << attackIndices.size();

        // For every pattern, for every valid attack, shift the pattern by attack and get the first bit (0 or 1), move that
        // bit by attack and put it in the list of all blocking patterns, watch Coding Adventure: Making a Better Chess
        // Bot, Magic Bitboards (minus the magic) for better understanding
        long[] blockingPatternsBitboards = new long[numPatterns];
        for (int patternIndex = 0; patternIndex < numPatterns; patternIndex += 1) {
            for (int attackIndex = 0; attackIndex < attackIndices.size(); attackIndex += 1) {
                long bit = (patternIndex >> attackIndex) & 1;
                blockingPatternsBitboards[patternIndex] |= bit << attackIndices.get(attackIndex);
            }
        }

        return blockingPatternsBitboards;
    }


    public long findMagicNumber(long[] blockingPatternsBitboards, long[] pseudoLegalMoves) {
        for (int i = 0; i < 10000000; i += 1) {
            // Generate a random magic number with not a lot of 1s
            Random rand = new Random();
            long magic = rand.nextLong() & rand.nextLong() & rand.nextLong();

            // Creates the used array, for each blocking pattern there is a starting value of -1, the goal is to fill most
            // of the array with distinct pseudo legal moves
            int bits = Long.bitCount(blockingPatternsBitboards.length);
            int size = 1 << bits;
            long[] used = new long[size];
            Arrays.fill(used, -1);

            // For every blocking pattern, generate an index, using the potential magic number with the below formula, if
            // the generated index doesn't point to an already used slot in the used array, it continues. If it's already
            // used and the pseudo legal moves in that slot are different from the current ones, it means that this
            // potential magic number doesn't fill the used array with distinct pseudo legal moves, so it's discarded
            boolean fail = false;
            for (int j = 0; j < blockingPatternsBitboards.length; j += 1) {
                int index = (int) ((blockingPatternsBitboards[j] * magic) >>> (64 - bits));

                if (used[index] == -1) {
                    used[index] = pseudoLegalMoves[j];
                }

                else if (used[index] != pseudoLegalMoves[j]) {
                    fail = true;
                    break;
                }
            }

            if (!fail) {
                return magic;
            }
        }

        return 0;
    }


    public void findBestMagicNumber() {
        // For the rook, for each square, get the valid attacks, from those get the blocking patterns, for every blocking
        // pattern, get the legal moves, from those get the magic number that corresponds to all blocking patterns in that
        // square
        for (int square = 0; square < 64; square += 1) {
            long attacksBitboard = Rook.attacks(square);
            long[] blockingPatternsBitboards = createBlockingPatternsBitboards(attacksBitboard);

            long[] pseudoLegalMoves = new long[blockingPatternsBitboards.length];
            for (int i = 0; i < blockingPatternsBitboards.length; i += 1) {
                pseudoLegalMoves[i] = Rook.pseudoLegalMoves(square, blockingPatternsBitboards[i]);
            }

            long magicNumber = findMagicNumber(blockingPatternsBitboards, pseudoLegalMoves);
            int bits = Long.bitCount(blockingPatternsBitboards.length);

            for (int i = 0; i < blockingPatternsBitboards.length; i += 1) {
                int index = (int) ((blockingPatternsBitboards[i] * magicNumber) >>> (64 - bits));
            }
        }
    }
}
