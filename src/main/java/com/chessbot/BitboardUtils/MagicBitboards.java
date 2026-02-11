package com.chessbot.BitboardUtils;

import com.chessbot.Objects.Pieces.Rook;
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


    public long findMagicNumber(long attacksBitboard, long[] blockingPatternsBitboards, long[] moves) {
        for (int k = 0; k < 100000000; k++) {
            Random rand = new Random();
            long magic = rand.nextInt();

            int bits = Long.bitCount(attacksBitboard);
            int size = 1 << bits;

            long[] used = new long[size];
            for(int i = 0; i < size; i += 1) {
                used[i] = -1;
            }

            boolean fail = false;

            for (int i = 0; i < blockingPatternsBitboards.length; i++) {
                long blockingPatternsBitboard = blockingPatternsBitboards[i];

                int index = (int) (blockingPatternsBitboard * magic) >> (64 - bits);

                if (used[index] == -1) {
                    used[index] = moves[i];
                }

                else if (used[index] != moves[i]) {
                    fail = true;
                    break;
                }
            }

            if (!fail) {
                return magic;
            }
        }

        System.out.println("Failed to find magic");
        return 0;
    }


    public void generateAllMagicNumbers() {
        for (int square = 0; square < 64; square++) {
            long attacksBitboard = Rook.attacks(square);
            long[] blockingPatternsBitboards = createBlockingPatternsBitboards(attacksBitboard);

            long[] pseudoLegalMoves = new long[blockingPatternsBitboards.length];
            for (int i = 0; i < blockingPatternsBitboards.length; i++) {
                pseudoLegalMoves[i] = Rook.pseudoLegalMoves(square, blockingPatternsBitboards[i]);
            }

            long magic = findMagicNumber(attacksBitboard, blockingPatternsBitboards, pseudoLegalMoves);
            System.out.printf("Square %d: 0x%XL\n", square, magic);
        }
    }


    public Map<Key, Long> createRookLookupTable() {
        // Create a HashMap to look up rook legal moves from the key: startingSquare, blockingPatternBitboard
        Map<Key, Long> rookMovesLookupTable = new HashMap<>();

        // For the rook, for each square, get the valid attacks, from those get the blocking patterns, for every blocking
        // pattern, get the legal moves for the sliding piece and put them in a HashMap
        for (int startingSquare = 0; startingSquare < 64; startingSquare += 1) {
            long attacksBitboard = Rook.attacks(startingSquare);
            long[] blockingPatternsBitboards = createBlockingPatternsBitboards(attacksBitboard);

            for (long blockingPatternBitboard : blockingPatternsBitboards) {
                long pseudoLegalMoves = Rook.pseudoLegalMoves(startingSquare, blockingPatternBitboard);
                rookMovesLookupTable.put(new Key(startingSquare, blockingPatternBitboard), pseudoLegalMoves);
            }
        }

        return rookMovesLookupTable;
    }
}
