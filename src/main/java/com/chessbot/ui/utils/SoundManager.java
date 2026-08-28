package com.chessbot.ui.utils;

import com.chessbot.application.ChessApplication;
import com.chessbot.engine.core.Move;
import javafx.scene.media.AudioClip;

// Separate class to not clutter the code
public final class SoundManager {
    private static final AudioClip checkSound = new AudioClip(ChessApplication.class.getResource("/com/chessbot/Sounds/check.mp3").toString());
    private static final AudioClip captureSound = new AudioClip(ChessApplication.class.getResource("/com/chessbot/Sounds/capture.mp3").toString());
    private static final AudioClip castleSound = new AudioClip(ChessApplication.class.getResource("/com/chessbot/Sounds/castle.mp3").toString());
    private static final AudioClip moveSound = new AudioClip(ChessApplication.class.getResource("/com/chessbot/Sounds/move.mp3").toString());
    private static final AudioClip promoteSound = new AudioClip(ChessApplication.class.getResource("/com/chessbot/Sounds/promote.mp3").toString());
    private static final AudioClip illegalSound = new AudioClip(ChessApplication.class.getResource("/com/chessbot/Sounds/illegal.mp3").toString());
    private static final AudioClip endSound = new AudioClip(ChessApplication.class.getResource("/com/chessbot/Sounds/end.mp3").toString());

    private SoundManager() {}


    // Determines which move sound to play based on priority
    public static void playMoveSound(boolean isCheck, int flag) {
        if (isCheck) {
            checkSound.play();
        }

        else if (flag == Move.FLAG_CAPTURE || flag == Move.FLAG_EN_PASSANT_CAPTURE) {
            captureSound.play();
        }

        else if (flag == Move.FLAG_KING_CASTLE || flag == Move.FLAG_QUEEN_CASTLE) {
            castleSound.play();
        }

        else if (flag >= Move.FLAG_KNIGHT_PROMOTION_CAPTURE) {
            promoteSound.play();
        }

        else {
            moveSound.play();
        }
    }


    public static void playIllegalSound() {
        illegalSound.play();
    }

    public static void playEndSound() { endSound.play(); }
}
