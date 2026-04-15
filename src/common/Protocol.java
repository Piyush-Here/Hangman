package common;

import java.util.List;

/**
 * Defines the text-based protocol used between GameServer and GameClient.
 * All messages are single-line strings (newline-terminated by PrintWriter).
 *
 * Format:  TAG|payload
 */
public class Protocol {

    // ── Tags ────────────────────────────────────────────────────────────────
    public static final String TAG_WELCOME      = "WELCOME";
    public static final String TAG_SERVER_INFO  = "INFO";
    public static final String TAG_GAME_START   = "START";
    public static final String TAG_YOUR_TURN    = "YOUR_TURN";
    public static final String TAG_WAIT         = "WAIT";
    public static final String TAG_BOARD        = "BOARD";
    public static final String TAG_GUESS_RESULT = "RESULT";
    public static final String TAG_GAME_OVER    = "OVER";
    public static final String TAG_ERROR        = "ERROR";
    public static final String PING             = "PING";
    public static final String PONG             = "PONG";
    public static final String GUESS_PREFIX     = "GUESS|";

    private static final String SEP = "|";

    // ── Server → Client builders ─────────────────────────────────────────────

    public static String welcome(int playerIndex) {
        return TAG_WELCOME + SEP + (playerIndex + 1);
    }

    public static String serverInfo(String msg) {
        return TAG_SERVER_INFO + SEP + msg;
    }

    public static String gameStart(GameState gs, boolean hard) {
        return TAG_GAME_START + SEP
                + gs.getWordLength() + SEP
                + (hard ? "HARD" : "EASY") + SEP
                + gs.getNumPlayers();
    }

    public static String boardUpdate(GameState gs) {
        // BOARD|revealed|wrongCount|wrongAllowed|hangmanArt|options
        String options = optionsString(gs.getCurrentOptions());
        String wrongs  = wrongsString(gs.getWrongGuesses());
        return TAG_BOARD + SEP
                + gs.getRevealedString() + SEP
                + gs.getWrongCount() + SEP
                + gs.getWrongAllowed() + SEP
                + encodeArt(gs.getHangmanArt()) + SEP
                + options + SEP
                + wrongs;
    }

    public static String yourTurn(GameState gs, int playerIndex) {
        String options = optionsString(gs.getCurrentOptions());
        return TAG_YOUR_TURN + SEP + (playerIndex + 1) + SEP + options;
    }

    public static String waitTurn(GameState gs, int currentPlayerIndex) {
        return TAG_WAIT + SEP + "Player " + (currentPlayerIndex + 1) + "'s turn";
    }

    public static String guessResult(GameState gs, String playerName, String guess, boolean correct) {
        return TAG_GUESS_RESULT + SEP
                + playerName + SEP
                + guess + SEP
                + (correct ? "CORRECT" : "WRONG") + SEP
                + gs.getRevealedString();
    }

    public static String gameOver(GameState gs) {
        String outcome = gs.isGameWon() ? "WIN" : "LOSE";
        StringBuilder sb = new StringBuilder();
        sb.append(TAG_GAME_OVER).append(SEP)
          .append(outcome).append(SEP)
          .append(gs.getWord()).append(SEP);

        int[] scores = gs.getScores();
        for (int i = 0; i < scores.length; i++) {
            sb.append("P").append(i + 1).append(":").append(scores[i]);
            if (i < scores.length - 1) sb.append(",");
        }
        return sb.toString();
    }

    public static String error(String msg) {
        return TAG_ERROR + SEP + msg;
    }

    // ── Client → Server builders ─────────────────────────────────────────────

    public static String makeGuess(String guess) {
        return GUESS_PREFIX + guess;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String optionsString(List<Character> options) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(options.get(i));
        }
        return sb.toString();
    }

    private static String wrongsString(List<Character> wrongs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wrongs.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(wrongs.get(i));
        }
        return sb.toString();
    }

    /** Replace newlines in hangman art so it fits on one protocol line */
    public static String encodeArt(String art) {
        return art.replace("\n", "\\n").replace("\r", "");
    }

    /** Reverse of encodeArt */
    public static String decodeArt(String encoded) {
        return encoded.replace("\\n", "\n");
    }

    public static String[] split(String message) {
        return message.split("\\|", -1);
    }
}
