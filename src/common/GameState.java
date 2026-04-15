package common;

import java.util.*;

/**
 * Holds all mutable game state:
 *  - The secret word
 *  - Which letters have been revealed
 *  - Whose turn it is
 *  - Scores per player
 *  - Option pool for each turn (the multiple-choice letters)
 *  - Wrong guesses tracking (for Easy vs Hard display)
 *  - Win/lose status
 */
public class GameState {

    // ── Constants ───────────────────────────────────────────────────────────
    private static final int BASE_WRONG_ALLOWED = 6;

    // ── Core word state ─────────────────────────────────────────────────────
    private final String word;
    private final char[] revealed;          // '_' until guessed
    private final Set<Character> guessedCorrect = new LinkedHashSet<>();
    private final List<Character> wrongGuesses  = new ArrayList<>();

    // ── Player state ────────────────────────────────────────────────────────
    private final int numPlayers;
    private int currentPlayerIndex = 0;
    private final int[] scores;

    // ── Mode ────────────────────────────────────────────────────────────────
    private final boolean hardMode;
    private final int wrongAllowed;

    // ── Options for current turn ─────────────────────────────────────────────
    private List<Character> currentOptions = new ArrayList<>();

    // ── Status ──────────────────────────────────────────────────────────────
    private boolean gameWon  = false;
    private boolean gameLost = false;

    // ────────────────────────────────────────────────────────────────────────

    public GameState(List<String> wordPool, int numPlayers, boolean hardMode) {
        Random rng = new Random();
        this.word       = wordPool.get(rng.nextInt(wordPool.size())).toLowerCase();
        this.numPlayers = numPlayers;
        this.hardMode   = hardMode;
        this.revealed   = new char[word.length()];
        this.scores     = new int[numPlayers];
        this.wrongAllowed = BASE_WRONG_ALLOWED; // 6 wrong guesses end the game

        Arrays.fill(revealed, '_');
        generateOptionsForTurn();
    }

    // ────────────────────────────────────── OPTION GENERATION ───────────────

    /**
     * Rule 2: N players → at most N-1 options (cap 26).
     * Rule 3: At least 1 option must be a correct unguessed letter.
     * Rule 4: In easy mode, already-wrong options are not repeated.
     *         In hard mode, they may reappear.
     */
    public void generateOptionsForTurn() {
        int maxOptions = Math.min(numPlayers - 1, 26);
        Set<Character> unguessedLetters = new LinkedHashSet<>();
        for (char c : word.toCharArray()) {
            if (!guessedCorrect.contains(c)) unguessedLetters.add(c);
        }

        // Build alphabet pool
        List<Character> alphabet = new ArrayList<>();
        for (char c = 'a'; c <= 'z'; c++) {
            if (guessedCorrect.contains(c)) continue;          // already revealed
            if (!hardMode && wrongGuesses.contains(c)) continue; // easy: hide wrong ones
            alphabet.add(c);
        }

        Collections.shuffle(alphabet);

        Set<Character> optionSet = new LinkedHashSet<>();

        // Rule 3: guarantee at least 1 correct option
        List<Character> correctList = new ArrayList<>(unguessedLetters);
        Collections.shuffle(correctList);
        if (!correctList.isEmpty()) optionSet.add(correctList.get(0));

        // Fill rest randomly up to maxOptions
        for (Character c : alphabet) {
            if (optionSet.size() >= maxOptions) break;
            optionSet.add(c);
        }

        // If we still have room and there are more correct letters, prioritize them
        for (Character c : correctList) {
            if (optionSet.size() >= maxOptions) break;
            optionSet.add(c);
        }

        currentOptions = new ArrayList<>(optionSet);
        Collections.shuffle(currentOptions); // shuffle so correct isn't always first
    }

    // ────────────────────────────────────── GUESS PROCESSING ─────────────────

    /**
     * Process a guess (single letter or full word).
     * Returns true if the guess was correct.
     */
    public boolean processGuess(String guess, int playerIndex) {
        guess = guess.trim().toLowerCase();

        if (guess.length() == 1) {
            return processLetterGuess(guess.charAt(0), playerIndex);
        } else {
            return processWordGuess(guess, playerIndex);
        }
    }

    private boolean processLetterGuess(char letter, int playerIndex) {
        // Verify it's in the current options
        if (!currentOptions.contains(letter)) return false;

        if (word.indexOf(letter) >= 0 && !guessedCorrect.contains(letter)) {
            guessedCorrect.add(letter);
            int occurrences = 0;
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == letter) {
                    revealed[i] = letter;
                    occurrences++;
                }
            }
            scores[playerIndex] += 10 * occurrences; // 10 pts per revealed letter
            checkWin();
        } else {
            wrongGuesses.add(letter);
            scores[playerIndex] = Math.max(0, scores[playerIndex] - 5); // penalty
            checkLoss();
        }

        generateOptionsForTurn();
        return guessedCorrect.contains(letter);
    }

    private boolean processWordGuess(String guess, int playerIndex) {
        if (guess.equals(word)) {
            // Reveal all
            for (int i = 0; i < word.length(); i++) revealed[i] = word.charAt(i);
            scores[playerIndex] += 50; // bonus for full word
            gameWon = true;
            return true;
        } else {
            wrongGuesses.add('?'); // marker for a wrong word guess
            scores[playerIndex] = Math.max(0, scores[playerIndex] - 10);
            checkLoss();
            generateOptionsForTurn();
            return false;
        }
    }

    private void checkWin() {
        for (char c : revealed) {
            if (c == '_') return;
        }
        gameWon = true;
    }

    private void checkLoss() {
        if (wrongGuesses.size() >= wrongAllowed) gameLost = true;
    }

    // ────────────────────────────────────── TURN MANAGEMENT ─────────────────

    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers;
        generateOptionsForTurn();
    }

    // ────────────────────────────────────── HANGMAN ASCII ────────────────────

    public String getHangmanArt() {
        int wrongs = getWrongCount();
        return switch (wrongs) {
            case 0 -> """
                    +---+
                    |   |
                        |
                        |
                        |
                        |
                    =========
                    """;
            case 1 -> """
                    +---+
                    |   |
                    O   |
                        |
                        |
                        |
                    =========
                    """;
            case 2 -> """
                    +---+
                    |   |
                    O   |
                    |   |
                        |
                        |
                    =========
                    """;
            case 3 -> """
                    +---+
                    |   |
                    O   |
                   /|   |
                        |
                        |
                    =========
                    """;
            case 4 -> """
                    +---+
                    |   |
                    O   |
                   /|\\  |
                        |
                        |
                    =========
                    """;
            case 5 -> """
                    +---+
                    |   |
                    O   |
                   /|\\  |
                   /    |
                        |
                    =========
                    """;
            default -> """
                    +---+
                    |   |
                    O   |
                   /|\\  |
                   / \\  |
                        |
                    =========
                    """;
        };
    }

    // ────────────────────────────────────── GETTERS ───────────────────────────

    public String getWord()                  { return word; }
    public int getWordLength()               { return word.length(); }
    public char[] getRevealed()              { return Arrays.copyOf(revealed, revealed.length); }
    public String getRevealedString()        { return new String(revealed); }
    public List<Character> getWrongGuesses() { return Collections.unmodifiableList(wrongGuesses); }
    public int getWrongCount()               { return wrongGuesses.size(); }
    public int getWrongAllowed()             { return wrongAllowed; }
    public int getCurrentPlayerIndex()       { return currentPlayerIndex; }
    public int getNumPlayers()               { return numPlayers; }
    public int[] getScores()                 { return Arrays.copyOf(scores, scores.length); }
    public boolean isHardMode()              { return hardMode; }
    public List<Character> getCurrentOptions(){ return Collections.unmodifiableList(currentOptions); }
    public boolean isGameWon()               { return gameWon; }
    public boolean isGameLost()              { return gameLost; }
    public boolean isFinished()              { return gameWon || gameLost; }
}
