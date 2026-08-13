package com.chessbot.ui.utils;

import com.chessbot.ChessApplication;
import com.chessbot.engine.core.Move;
import javafx.scene.media.AudioClip;

// Separate class to not clutter the code
public final class SoundManager {
    private static final AudioClip moveSound = new AudioClip(ChessApplication.class.getResource("Sounds/move.mp3").toString());
    private static final AudioClip checkSound = new AudioClip(ChessApplication.class.getResource("Sounds/check.mp3").toString());
    private static final AudioClip captureSound = new AudioClip(ChessApplication.class.getResource("Sounds/capture.mp3").toString());
    private static final AudioClip illegalSound = new AudioClip(ChessApplication.class.getResource("Sounds/illegal.mp3").toString());

    private SoundManager() {}


    // Determines which move sound to play based on priority
    public static void playMoveSound(boolean isCheck, int flag) {
        if (isCheck) {
            checkSound.play();
        }

        else if (flag == Move.FLAG_CAPTURE || flag == Move.FLAG_EN_PASSANT) {
            captureSound.play();
        }

        else {
            moveSound.play();
        }
    }


    public static void playIllegalSound() {
        illegalSound.play();
    }
}
