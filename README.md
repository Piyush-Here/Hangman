# Hangman (Java)

[![CI](../../actions/workflows/ci.yml/badge.svg)](../../actions/workflows/ci.yml)

A terminal Hangman game written in plain Java 17 with no dependencies. A random word is picked from `Words.txt`; you have 6 wrong guesses to find it, and the hangman is drawn in ASCII as you miss.

## Run it

Requires **JDK 17 or newer**. From the project folder:

```bash
javac -d out src/*.java
java -cp out Main
```

To use another word list: `java -cp out Main path/to/words.txt` (one lowercase word per line).

## How to play

Each turn, pick an action:

1. **Guess a letter** - correct letters are revealed in the word; wrong ones cost a chance.
2. **Guess the word** - correct wins immediately; wrong costs a chance.

You win by revealing every letter or guessing the word. You lose after 6 wrong guesses.

Repeated letters, non-letters and bad menu input are rejected with a message and never cost a chance.

## Design

| File | Responsibility |
|---|---|
| `src/HangmanGame.java` | Rules and state of one round. No console I/O, so it is unit-testable. |
| `src/Main.java` | Console UI: loads words, reads input, prints output. |
| `src/HangmanArt.java` | The six ASCII-art stages. |
| `test/HangmanGameTest.java` | Dependency-free tests of the game rules. |

## Tests

```bash
javac -d out src/*.java test/*.java
java -ea -cp out HangmanGameTest
```

GitHub Actions runs the same on every push.

## License

MIT
