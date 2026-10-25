/**
 * Dependency-free tests. Run with:  java -ea -cp out HangmanGameTest
 * Exits with a non-zero status if any check fails (so CI can use it).
 */
public class HangmanGameTest {
    private static int failures = 0;

    private static void check(String name, boolean condition) {
        System.out.println((condition ? "PASS  " : "FAIL  ") + name);
        if (!condition) failures++;
    }

    public static void main(String[] args) {
        HangmanGame g = new HangmanGame("able");
        check("masked word starts hidden", g.maskedWord().equals("_ _ _ _"));
        check("correct letter is accepted", g.guessLetter('a') == HangmanGame.GuessResult.CORRECT);
        check("masked word reveals the letter", g.maskedWord().equals("a _ _ _"));
        check("letters are case-insensitive", g.guessLetter('B') == HangmanGame.GuessResult.CORRECT);
        check("repeated letter is flagged, not penalised", g.guessLetter('a') == HangmanGame.GuessResult.ALREADY_GUESSED && g.wrongGuesses() == 0);
        check("non-letter is invalid, not penalised", g.guessLetter('7') == HangmanGame.GuessResult.INVALID && g.wrongGuesses() == 0);
        g.guessLetter('l');
        check("still in progress before the last letter", g.status() == HangmanGame.Status.IN_PROGRESS);
        g.guessLetter('e');
        check("revealing every letter wins the game", g.status() == HangmanGame.Status.WON);

        HangmanGame lose = new HangmanGame("able");
        for (char c : "zxqwjk".toCharArray()) lose.guessLetter(c);
        check("six wrong letters lose the game", lose.status() == HangmanGame.Status.LOST && lose.remainingChances() == 0);

        HangmanGame repeat = new HangmanGame("able");
        repeat.guessLetter('z');
        repeat.guessLetter('z');
        check("same wrong letter only costs one chance", repeat.wrongGuesses() == 1);

        HangmanGame wordGuess = new HangmanGame("able");
        check("wrong word costs a chance", wordGuess.guessWord("cable") == HangmanGame.GuessResult.WRONG && wordGuess.wrongGuesses() == 1);
        check("correct word wins", wordGuess.guessWord("ABLE") == HangmanGame.GuessResult.CORRECT && wordGuess.status() == HangmanGame.Status.WON);
        check("invalid word input is rejected without penalty", new HangmanGame("able").guessWord("a b") == HangmanGame.GuessResult.INVALID);

        boolean threw = false;
        try { g.guessLetter('x'); } catch (IllegalStateException e) { threw = true; }
        check("guessing after the game is over is rejected", threw);

        boolean badWord = false;
        try { new HangmanGame("it's"); } catch (IllegalArgumentException e) { badWord = true; }
        check("constructor rejects non-letter words", badWord);

        System.out.println(failures == 0 ? "\nAll checks passed." : "\n" + failures + " check(s) FAILED.");
        System.exit(failures == 0 ? 0 : 1);
    }
}
