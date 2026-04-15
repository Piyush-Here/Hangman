package client;

import common.Protocol;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * HangmanMultiplayer - GameClient
 * Players run this on their own machine and connect to the host's IP.
 *
 * Usage: java -cp out client.GameClient <host-ip> <port>
 */
public class GameClient {

    private final String host;
    private final int port;
    private int myPlayerNumber = -1;
    private boolean myTurn = false;

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║    HANGMAN MULTIPLAYER - PLAYER      ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("Connecting to " + host + ":" + port + " ...");

        try (
            Socket socket = new Socket(host, port);
            BufferedReader serverIn  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter    serverOut = new PrintWriter(socket.getOutputStream(), true);
            Scanner        userIn    = new Scanner(System.in)
        ) {
            System.out.println("✅ Connected!\n");

            // Separate thread reads server messages and prints them
            Thread readerThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = serverIn.readLine()) != null) {
                        handleServerMessage(line, serverOut, userIn);
                    }
                } catch (IOException e) {
                    System.out.println("\n❌ Disconnected from server.");
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            // Keep main thread alive while reader runs
            readerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\nGame session ended. Thanks for playing! 🎮");
    }

    private void handleServerMessage(String line, PrintWriter serverOut, Scanner userIn) {
        String[] parts = Protocol.split(line);
        String tag = parts[0];

        switch (tag) {
            case Protocol.TAG_WELCOME -> {
                myPlayerNumber = Integer.parseInt(parts[1]);
                System.out.println("👋 You are Player " + myPlayerNumber);
                System.out.println("Waiting for the host to start the game...\n");
            }

            case Protocol.TAG_SERVER_INFO -> {
                System.out.println("ℹ️  " + parts[1]);
            }

            case Protocol.TAG_GAME_START -> {
                int wordLen   = Integer.parseInt(parts[1]);
                String mode   = parts[2];
                int players   = Integer.parseInt(parts[3]);
                System.out.println("\n╔════════════════════════════════════╗");
                System.out.printf ("║  🎮 GAME STARTED  [%s MODE]%n", mode);
                System.out.printf ("║  Players: %-3d  Word length: %-3d%n", players, wordLen);
                System.out.println("╚════════════════════════════════════╝");
            }

            case Protocol.TAG_BOARD -> {
                // BOARD|revealed|wrongCount|wrongAllowed|hangmanArt|options|wrongs
                String revealed   = parts[1];
                int wrongCount    = Integer.parseInt(parts[2]);
                int wrongAllowed  = Integer.parseInt(parts[3]);
                String artEncoded = parts[4];
                String optionsRaw = parts.length > 5 ? parts[5] : "";
                String wrongsRaw  = parts.length > 6 ? parts[6] : "";

                System.out.println();
                System.out.println(Protocol.decodeArt(artEncoded));
                System.out.println("Word:  " + formatRevealed(revealed));
                System.out.println("Wrong guesses: " + wrongCount + "/" + wrongAllowed
                        + (wrongsRaw.isEmpty() ? "" : "  [" + wrongsRaw + "]"));
            }

            case Protocol.TAG_YOUR_TURN -> {
                // YOUR_TURN|playerNum|optionA,optionB,...
                String optionsRaw = parts[2];
                String[] opts = optionsRaw.isEmpty() ? new String[0] : optionsRaw.split(",");

                myTurn = true;
                System.out.println("\n🟢 ══ IT'S YOUR TURN! ══");
                System.out.println("Available letters:");
                printOptions(opts);
                System.out.println("\nEnter a letter from the list above (or type a full word to guess): ");
                System.out.print(">>> ");

                // Read input (blocking - this is on the reader thread, but input is from userIn)
                String guess = "";
                try {
                    guess = userIn.nextLine().trim().toLowerCase();
                } catch (Exception e) {
                    System.out.println("Input error.");
                }

                if (!guess.isEmpty()) {
                    // Validate if it's a letter guess (must be from options list)
                    boolean valid = true;
                    if (guess.length() == 1) {
                        valid = false;
                        for (String opt : opts) {
                            if (opt.equals(guess)) { valid = true; break; }
                        }
                        if (!valid) {
                            System.out.println("⚠️  That letter is not in your options! Pick from the list.");
                            // Re-prompt: server will re-send YOUR_TURN if we don't respond,
                            // but for simplicity we'll send anyway and let server validate
                        }
                    }
                    serverOut.println(Protocol.makeGuess(guess));
                }
                myTurn = false;
            }

            case Protocol.TAG_WAIT -> {
                System.out.println("\n⏳ " + parts[1] + " — Waiting...");
            }

            case Protocol.TAG_GUESS_RESULT -> {
                // RESULT|playerName|guess|CORRECT/WRONG|revealed
                String player  = parts[1];
                String guess   = parts[2];
                String outcome = parts[3];
                String revealed = parts[4];

                String emoji = outcome.equals("CORRECT") ? "✅" : "❌";
                System.out.printf("%n%s  %s guessed '%s' → %s%n", emoji, player, guess, outcome);
                System.out.println("Word: " + formatRevealed(revealed));
            }

            case Protocol.TAG_GAME_OVER -> {
                // OVER|WIN/LOSE|word|P1:score,P2:score,...
                String outcome = parts[1];
                String word    = parts[2];
                String scores  = parts.length > 3 ? parts[3] : "";

                System.out.println("\n╔══════════════════════════════════════╗");
                if (outcome.equals("WIN")) {
                    System.out.println("║       🏆  WORD GUESSED! YOU WIN!     ║");
                } else {
                    System.out.println("║       ☠️   GAME OVER! YOU LOST!       ║");
                }
                System.out.println("║  The word was: " + word.toUpperCase());
                System.out.println("╠══════════════════════════════════════╣");
                System.out.println("║  FINAL SCORES:");
                for (String s : scores.split(",")) {
                    System.out.println("║    " + s.replace(":", " → "));
                }
                System.out.println("╚══════════════════════════════════════╝");
            }

            case Protocol.TAG_ERROR -> {
                System.out.println("⚠️  Server: " + parts[1]);
            }

            case Protocol.PONG -> { /* heartbeat reply, ignore */ }

            default -> {
                // Unknown message, print raw for debugging
                System.out.println("[server]: " + line);
            }
        }
    }

    /** Formats "_p_l_e" nicely with spaces */
    private String formatRevealed(String raw) {
        StringBuilder sb = new StringBuilder();
        for (char c : raw.toCharArray()) {
            sb.append(c == '_' ? '_' : c).append(' ');
        }
        return sb.toString().trim();
    }

    /** Prints lettered options in a nice grid */
    private void printOptions(String[] opts) {
        System.out.println("┌──────────────────────────────┐");
        StringBuilder row = new StringBuilder("│  ");
        for (int i = 0; i < opts.length; i++) {
            row.append("[").append(opts[i].toUpperCase()).append("]  ");
            if ((i + 1) % 6 == 0 || i == opts.length - 1) {
                // Pad row to fixed width
                while (row.length() < 33) row.append(' ');
                System.out.println(row + "│");
                row = new StringBuilder("│  ");
            }
        }
        System.out.println("└──────────────────────────────┘");
    }

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port    = args.length > 1 ? Integer.parseInt(args[1]) : 5555;
        new GameClient(host, port).connect();
    }
}
