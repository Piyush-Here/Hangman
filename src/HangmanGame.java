import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The rules and state of one Hangman round. Contains no input/output, so it can be tested on its own.
 */
public class HangmanGame {
    public enum Status { IN_PROGRESS, WON, LOST }

    public enum GuessResult { CORRECT, WRONG, ALREADY_GUESSED, INVALID }

    public static final int MAX_WRONG_GUESSES = 6;

    private final String word;
    private final Set<Character> correctLetters = new LinkedHashSet<>();
    private final Set<Character> wrongLetters = new LinkedHashSet<>();
    private int wrongGuesses = 0; // wrong letters AND wrong whole-word guesses both cost a chance
    private Status status = Status.IN_PROGRESS;

    public HangmanGame(String word) {
        if (word == null || !word.matches("[A-Za-z]+")) {
            throw new IllegalArgumentException("word must contain letters a-z only");
        }
        this.word = word.toLowerCase();
    }

    /** Guess a single letter. Repeated or invalid guesses never cost a chance. */
    public GuessResult guessLetter(char letter) {
        requireInProgress();
        char c = Character.toLowerCase(letter);
        if (c < 'a' || c > 'z') {
            return GuessResult.INVALID;
        }
        if (correctLetters.contains(c) || wrongLetters.contains(c)) {
            return GuessResult.ALREADY_GUESSED;
        }
        if (word.indexOf(c) >= 0) {
            correctLetters.add(c);
            if (isWordRevealed()) {
                status = Status.WON;
            }
            return GuessResult.CORRECT;
        }
        wrongLetters.add(c);
        registerWrongGuess();
        return GuessResult.WRONG;
    }

    /** Guess the whole word. A wrong guess costs one chance. */
    public GuessResult guessWord(String guess) {
        requireInProgress();
        if (guess == null || !guess.matches("[A-Za-z]+")) {
            return GuessResult.INVALID;
        }
        if (guess.toLowerCase().equals(word)) {
            status = Status.WON;
            return GuessResult.CORRECT;
        }
        registerWrongGuess();
        return GuessResult.WRONG;
    }

    private void registerWrongGuess() {
        wrongGuesses++;
        if (wrongGuesses >= MAX_WRONG_GUESSES) {
            status = Status.LOST;
        }
    }

    private boolean isWordRevealed() {
        for (int i = 0; i < word.length(); i++) {
            if (!correctLetters.contains(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void requireInProgress() {
        if (status != Status.IN_PROGRESS) {
            throw new IllegalStateException("The game is already over");
        }
    }

    /** The word with unguessed letters hidden, e.g. "_ a r _ e n". */
    public String maskedWord() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            char c = word.charAt(i);
            sb.append(correctLetters.contains(c) ? c : '_');
        }
        return sb.toString();
    }

    public Status status() { return status; }

    public int wordLength() { return word.length(); }

    public int wrongGuesses() { return wrongGuesses; }

    public int remainingChances() { return MAX_WRONG_GUESSES - wrongGuesses; }

    public Set<Character> wrongLetters() { return java.util.Collections.unmodifiableSet(wrongLetters); }

    /** The secret word. Only call this once the game is over. */
    public String revealWord() { return word; }
}
