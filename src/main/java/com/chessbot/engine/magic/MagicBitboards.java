package com.chessbot.engine.magic;

import java.util.*;

public abstract class MagicBitboards {
    protected abstract long getAttacks(int square);

    protected abstract long getPseudoLegalMoves(int square, long blockingPatternsBitboard);


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

        // For every pattern, for every valid attack, shift the pattern by attack and get the first bit (0 or 1), move
        // that bit by attack and put it in the list of all blocking patterns, watch Coding Adventure: Making a Better
        // Chess Bot, Magic Bitboards (minus the magic) for better understanding
        long[] blockingPatternsBitboards = new long[numPatterns];
        for (int patternIndex = 0; patternIndex < numPatterns; patternIndex += 1) {
            for (int attackIndex = 0; attackIndex < attackIndices.size(); attackIndex += 1) {
                long bit = (patternIndex >> attackIndex) & 1;
                blockingPatternsBitboards[patternIndex] |= bit << attackIndices.get(attackIndex);
            }
        }

        return blockingPatternsBitboards;
    }


    // Watch Coding Adventure: Making a Better Chess Bot, The Magical Part of Magic Bitboards to understand everything
    // below from here
    public long findMagicNumber(long[] blockingPatternsBitboards, long[] pseudoLegalMoves, int requestedBits) {
        Random rand = new Random();

        for (int i = 0; i < 10000000; i += 1) {
            // Generates a random magic number with not a lot of 1s
            long magic = rand.nextLong() & rand.nextLong() & rand.nextLong();

            // Creates the used array, for each blocking pattern there is a starting value of -1, the goal is to fill
            // most of the array with distinct pseudo legal moves, in the least amount of space
            long[] used = new long[1 << requestedBits];
            Arrays.fill(used, -1);

            // For every blocking pattern generate an index using the potential magic number with the below formula, if
            // the generated index doesn't point to an already used slot in the used array, it continues. If it's already
            // used and the pseudo legal moves in that slot are different from the current ones, it means that this
            // potential magic number doesn't fill the used array with distinct pseudo legal moves, so it's discarded
            boolean fail = false;
            for (int j = 0; j < blockingPatternsBitboards.length; j += 1) {
                int magicIndex = (int) ((blockingPatternsBitboards[j] * magic) >>> (64 - requestedBits));

                if (used[magicIndex] == -1) {
                    used[magicIndex] = pseudoLegalMoves[j];
                }

                else if (used[magicIndex] != pseudoLegalMoves[j]) {
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


    private long[] generatePseudoLegalMovesArray(int square, long[] blockingPatternsBitboards) {
        long[] pseudoLegalMoves = new long[blockingPatternsBitboards.length];
        for (int i = 0; i < blockingPatternsBitboards.length; i += 1) {
            pseudoLegalMoves[i] = getPseudoLegalMoves(square, blockingPatternsBitboards[i]);
        }

        return pseudoLegalMoves;
    }


    public void findBestMagicNumbers() {
        for (int square = 0; square < 64; square += 1) {
            long attacksBitboard = getAttacks(square);
            long[] blockingPatternsBitboards = createBlockingPatternsBitboards(attacksBitboard);
            long[] pseudoLegalMoves = generatePseudoLegalMovesArray(square, blockingPatternsBitboards);

            // Now we are trying to find the best magic number, maxBits = the number of squares the sliding piece on the
            // current square attacks, the bits number will determine the size of the movesLookupTable for this square. Bits
            // along with the magic number need to be optimized, minimum bits for rooks is 10 and maximum is 12, minimum
            // bits for bishops is 5 and maximum is 9, so it uses 5 for both
            long magicNumber = 0;
            int bestBits = 0;
            int maxBits = Long.bitCount(attacksBitboard);
            for (int bits = 5; bits <= maxBits; bits += 1) {
                magicNumber = findMagicNumber(blockingPatternsBitboards, pseudoLegalMoves, bits);

                if (magicNumber != 0) {
                    bestBits = bits;
                    break;
                }
            }

            // Prints the magic number and the best bits, so I can write them down, they are needed for later
            System.out.printf("Square: %d | Magic: %d | Bits: %d%n", square, magicNumber, bestBits);
        }
    }


    // Creates an 1D array to look up the legal moves of the sliding piece, the key is: offset + magicIndex
    public long[] createMovesLookupTable(long[] magicNumbers, int[] bestBits, int[] offsets) {
        int totalSize = 0;
        for (int bits : bestBits) {
            totalSize += (1 << bits);
        }

        long[] movesLookupTable = new long[totalSize];

        for (int square = 0; square < 64; square += 1) {
            long attacksBitboard = getAttacks(square);
            long[] blockingPatternsBitboards = createBlockingPatternsBitboards(attacksBitboard);
            long[] pseudoLegalMoves = generatePseudoLegalMovesArray(square, blockingPatternsBitboards);

            // Fills most of the optimal lookup table with distinct pseudo legal moves
            for (int i = 0; i < blockingPatternsBitboards.length; i += 1) {
                int magicIndex = (int) ((blockingPatternsBitboards[i] * magicNumbers[square]) >>> 64 - bestBits[square]);
                movesLookupTable[offsets[square] + magicIndex] = pseudoLegalMoves[i];
            }
        }

        return movesLookupTable;
    }
}
