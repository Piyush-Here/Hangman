import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * Terminal Hangman. Usage: java -cp out Main [path/to/Words.txt]
 * Handles all input/output; the rules live in {@link HangmanGame}.
 */
public class Main {

    public static void main(String[] args) {
        Path wordsFile = Path.of(args.length > 0 ? args[0] : "Words.txt");
        List<String> words;
        try {
            words = loadWords(wordsFile);
        } catch (IOException e) {
            System.err.println("Could not read the word list: " + wordsFile.toAbsolutePath());
            System.err.println("Run the game from the project folder (where Words.txt is), or pass the file path as an argument.");
            System.exit(1);
            return;
        }
        if (words.isEmpty()) {
            System.err.println("The word list is empty: " + wordsFile.toAbsolutePath());
            System.exit(1);
            return;
        }

        String secret = words.get(new Random().nextInt(words.size()));
        play(new HangmanGame(secret), new Scanner(System.in));
    }

    /** Reads one word per line; ignores blank/invalid lines and duplicates. */
    static List<String> loadWords(Path file) throws IOException {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String line : Files.readAllLines(file)) {
            String w = line.trim().toLowerCase();
            if (w.matches("[a-z]+")) {
                unique.add(w);
            }
        }
        return new ArrayList<>(unique);
    }

    static void play(HangmanGame game, Scanner in) {
        System.out.println("******************");
        System.out.println("Welcome to Hangman");
        System.out.println("******************");
        System.out.printf("%nYou have %d chances to guess the word.%n", HangmanGame.MAX_WRONG_GUESSES);
        System.out.printf("The word has %d letters.%n", game.wordLength());
        System.out.println(game.maskedWord());

        try {
            while (game.status() == HangmanGame.Status.IN_PROGRESS) {
                System.out.print("\nChoose your action:\n1. Guess a letter\n2. Guess the word\n: ");
                switch (in.nextLine().trim()) {
                    case "1" -> guessLetter(game, in);
                    case "2" -> guessWord(game, in);
                    default -> System.out.println("Please enter 1 or 2.");
                }
            }
        } catch (NoSuchElementException e) { // input stream closed (Ctrl+D / Ctrl+Z)
            System.out.println("\nInput closed. Goodbye!");
            return;
        }

        if (game.status() == HangmanGame.Status.WON) {
            System.out.println("\nYOU WIN! The word was " + game.revealWord() + ".");
        } else {
            System.out.println("\nYOU LOSE! The word was " + game.revealWord() + ".");
        }
    }

    private static void guessLetter(HangmanGame game, Scanner in) {
        System.out.print("Enter a letter: ");
        String line = in.nextLine().trim();
        if (line.length() != 1) {
            System.out.println("Please enter exactly one letter.");
            return;
        }
        char letter = Character.toLowerCase(line.charAt(0));
        HangmanGame.GuessResult result = game.guessLetter(letter);
        switch (result) {
            case INVALID -> System.out.println("Letters a-z only.");
            case ALREADY_GUESSED -> System.out.println("You already tried '" + letter + "'. No chance used.");
            case CORRECT -> System.out.println("Good guess!");
            case WRONG -> showWrong(game);
        }
        if (result == HangmanGame.GuessResult.CORRECT || result == HangmanGame.GuessResult.WRONG) {
            showStatus(game);
        }
    }

    private static void guessWord(HangmanGame game, Scanner in) {
        System.out.print("Enter the word: ");
        HangmanGame.GuessResult result = game.guessWord(in.nextLine().trim());
        switch (result) {
            case INVALID -> System.out.println("Letters a-z only.");
            case WRONG -> {
                showWrong(game);
                showStatus(game);
            }
            default -> { /* CORRECT: the win message is printed when the loop ends */ }
        }
    }

    private static void showWrong(HangmanGame game) {
        System.out.println("Wrong!");
        System.out.println(HangmanArt.forWrongGuesses(game.wrongGuesses()));
    }

    private static void showStatus(HangmanGame game) {
        if (game.status() != HangmanGame.Status.IN_PROGRESS) {
            return;
        }
        System.out.println(game.maskedWord());
        System.out.println("Wrong letters: " + (game.wrongLetters().isEmpty() ? "-" : game.wrongLetters()));
        System.out.println("Chances left: " + game.remainingChances());
    }
}
