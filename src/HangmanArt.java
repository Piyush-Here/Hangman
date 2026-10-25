/**
 * ASCII-art hangman stages. Stage n is shown after the n-th wrong guess.
 */
public final class HangmanArt {
    private HangmanArt() {}

    private static final String[] STAGES = {
        """
                -------------------------
                            |
                            |
                            |
                            O
               """,
        """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                            .
                            .
                            .
                            .
               """,
        """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                          / .
                         /  .
                            .
                            .
               """,
        """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                          / . \\
                         /  .  \\
                            .
                            .
               """,
        """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                          / . \\
                         /  .  \\
                            .
                            .
                          /
                         /
                        /
              """,
        """
                -------------------------
                            |
                            |
                            |
                            O
                            .
                          / . \\
                         /  .  \\
                            .
                            .
                          /   \\
                         /     \\
                        /       \\
              """
    };

    /** @param wrongGuesses number of wrong guesses so far (1..6) */
    public static String forWrongGuesses(int wrongGuesses) {
        if (wrongGuesses < 1 || wrongGuesses > STAGES.length) {
            throw new IllegalArgumentException("wrongGuesses must be 1.." + STAGES.length);
        }
        return STAGES[wrongGuesses - 1];
    }
}
