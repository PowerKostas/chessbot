package com.chessbot.engine.search.moveordering;

import com.chessbot.engine.core.Move;

public final class Promotions {
    // The score that a pawn, knight, bishop, rook or queen promotion gets. A pawn promoting to a pawn obviously can't happen
    // but its just there so the other indexes work. MVV-LVA scores range from 4 to 40. The philosophy is: Underpromoting
    // to a knight/bishop/rook is almost always a bad move. Even if the MVV-LVA scores are added to these scores, an underpromotion
    // will still get a negative value, and consequently it will be searched after all the quiet moves. It's a bit more common
    // for a knight underpromotion to be a good move compared to a bishop/rook underpromotion, so it gets a slightly higher
    // score. A queen promotion gives a material advantage of 8 while a pawn capturing a queen gives a material advantage
    // of 9. For that reason a queen promotion gets a slightly lower score than a pawn capturing a queen in MVV-LVA (score
    // of 40). The scores are used in other parts of the code, so try not tweak them
    private static final int[] PIECE_SCORES = {0, -45, -50, -50, 35};

    private Promotions() {}


    // If it's a promotion move, the promoted piece type is returned as the score
    public static int scoreMove(int move) {
        int flag = Move.getFlag(move);

        if (flag >= Move.FLAG_KNIGHT_PROMOTION) {
            int promotedPiece = (flag & 3) + 1;
            return PIECE_SCORES[promotedPiece];
        }

        return 0;
    }
}
