package server;

import common.GameState;
import common.Protocol;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * HangmanMultiplayer - GameServer
 * The host runs this. Clients connect via TCP on the LAN/hotspot/internet.
 */
public class GameServer {

    private final int port;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private GameState gameState;
    private final Object turnLock = new Object();
    private volatile boolean gameRunning = false;

    public GameServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║     HANGMAN MULTIPLAYER - HOST       ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("Server started on port " + port);
        printLocalIP();
        System.out.println("Waiting for players to join...");
        System.out.println("(Minimum 3 players required to start)\n");

        // Accept clients in a separate thread, allow host to type START
        Thread acceptThread = new Thread(() -> {
            while (!gameRunning) {
                try {
                    serverSocket.setSoTimeout(1000);
                    try {
                        Socket socket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(socket, this);
                        clients.add(handler);
                        new Thread(handler).start();
                        System.out.println("[+] Player connected: " + socket.getInetAddress().getHostAddress()
                                + " | Total players: " + clients.size());
                        broadcastToAll(Protocol.serverInfo("Player joined! Total: " + clients.size()));
                    } catch (SocketTimeoutException ignored) {}
                } catch (IOException e) {
                    if (!gameRunning) System.out.println("Accept error: " + e.getMessage());
                }
            }
            // Continue accepting after game starts (late joiners become spectators in future versions)
        });
        acceptThread.setDaemon(true);
        acceptThread.start();

        // Host controls game start
        Scanner hostScanner = new Scanner(System.in);
        while (true) {
            System.out.print("Host> Type START to begin (need ≥3 players): ");
            String cmd = hostScanner.nextLine().trim().toUpperCase();
            if (cmd.equals("START")) {
                if (clients.size() < 3) {
                    System.out.println("❌ Need at least 3 players. Currently: " + clients.size());
                } else {
                    break;
                }
            }
        }

        // Setup game
        System.out.println("\n--- GAME SETUP ---");
        System.out.print("Choose mode (1=Easy, 2=Hard): ");
        int modeChoice = Integer.parseInt(hostScanner.nextLine().trim());
        boolean hardMode = (modeChoice == 2);

        System.out.print("Custom word file path? (press Enter to use default): ");
        String customPath = hostScanner.nextLine().trim();

        List<String> wordPool = loadWords(customPath, hardMode);
        if (wordPool.isEmpty()) {
            System.out.println("❌ Not enough words. Using built-in fallback.");
            wordPool = getFallbackWords(hardMode);
        }

        // Initialize GameState
        int numPlayers = clients.size();
        gameState = new GameState(wordPool, numPlayers, hardMode);
        gameRunning = true;

        broadcastToAll(Protocol.gameStart(gameState, hardMode));
        System.out.println("\n🎮 Game started! Word length: " + gameState.getWordLength());
        System.out.println("Mode: " + (hardMode ? "HARD" : "EASY"));
        System.out.println("Players: " + numPlayers);

        runGameLoop(hostScanner);
    }

    /** Core turn-based game loop */
    private void runGameLoop(Scanner hostScanner) {
        while (!gameState.isFinished()) {
            int currentPlayerIndex = gameState.getCurrentPlayerIndex();
            ClientHandler currentClient = clients.get(currentPlayerIndex);

            // Announce whose turn it is
            String turnMsg = Protocol.yourTurn(gameState, currentPlayerIndex);
            String waitMsg = Protocol.waitTurn(gameState, currentPlayerIndex);

            currentClient.send(turnMsg);
            for (int i = 0; i < clients.size(); i++) {
                if (i != currentPlayerIndex) clients.get(i).send(waitMsg);
            }
            broadcastBoard();

            // Wait for this client's response (blocking via synchronized)
            synchronized (turnLock) {
                try {
                    turnLock.wait(60_000); // 60s timeout per turn
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (!gameState.isFinished()) {
                gameState.advanceTurn();
            }
        }

        // Game over broadcast
        broadcastToAll(Protocol.gameOver(gameState));
        System.out.println("\n🏁 Game Over! Word was: " + gameState.getWord());
        printScoreboard();
    }

    /** Called by ClientHandler when a valid move arrives */
    public synchronized void processGuess(int playerIndex, String guess) {
        if (playerIndex != gameState.getCurrentPlayerIndex()) {
            clients.get(playerIndex).send(Protocol.error("Not your turn!"));
            return;
        }

        boolean correct = gameState.processGuess(guess, playerIndex);
        String result = correct ? "✅ Correct!" : "❌ Wrong!";

        // Announce result to all
        String playerName = "Player " + (playerIndex + 1);
        broadcastToAll(Protocol.guessResult(gameState, playerName, guess, correct));
        broadcastBoard();

        if (gameState.isFinished()) {
            broadcastToAll(Protocol.gameOver(gameState));
        }

        synchronized (turnLock) {
            turnLock.notifyAll();
        }
    }

    private void broadcastBoard() {
        broadcastToAll(Protocol.boardUpdate(gameState));
    }

    public void broadcastToAll(String message) {
        for (ClientHandler c : clients) c.send(message);
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("[-] A player disconnected. Remaining: " + clients.size());
    }

    public GameState getGameState() { return gameState; }
    public boolean isGameRunning() { return gameRunning; }
    public Object getTurnLock() { return turnLock; }

    private List<String> loadWords(String path, boolean hardMode) {
        List<String> all = new ArrayList<>();
        if (path == null || path.isEmpty()) return all;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty() && line.matches("[a-z]+")) {
                    if (hardMode && line.length() >= 7) all.add(line);
                    else if (!hardMode && line.length() <= 6) all.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read custom word file: " + e.getMessage());
        }
        if (all.size() < 1000) {
            System.out.println("⚠️  Custom word set has " + all.size() + " valid words (need 1000). Using default.");
            all.clear();
        }
        return all;
    }

    private List<String> getFallbackWords(boolean hardMode) {
        // Minimal embedded fallback — in practice Words.txt ships with the jar
        String[] easy = {"apple","brave","crane","drift","ember","flame","grace","haste","irony",
                "joker","knack","lance","maple","nerve","opera","plume","quest","river","storm",
                "truce","ultra","vapor","waltz","xerox","yacht","zebra","blade","climb","depth",
                "eager","fable","grind","house","index","judge","kings","light","month","night",
                "orbit","pearl","quirk","renal","shard","tiger","unity","viola","wrath","young"};
        String[] hard = {"abandon","absence","achieve","acquire","balance","blanket","cabinet",
                "captain","comfort","diamond","dynasty","elegant","fantasy","general","harvest",
                "jealous","kitchen","lantern","machine","narrate","operate","pattern","qualify",
                "rainbow","science","tobacco","uniform","vehicle","warrior","younger","zealous",
                "abolish","ancient","beneath","blossom","century","confirm","destiny","eclipse",
                "failure","glimpse","history","impulse","jewelry","kingdom","liberty","mystery"};
        List<String> list = new ArrayList<>();
        String[] src = hardMode ? hard : easy;
        // Repeat to simulate large pool (real game uses Words.txt)
        for (int i = 0; i < 25; i++) Collections.addAll(list, src);
        return list;
    }

    private void printLocalIP() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface ni : Collections.list(nets)) {
                if (!ni.isLoopback() && ni.isUp()) {
                    for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                        if (addr instanceof Inet4Address) {
                            System.out.println("📡 Your LAN IP: " + addr.getHostAddress()
                                    + "  → share this with players!");
                        }
                    }
                }
            }
        } catch (SocketException e) { System.out.println("Could not detect IP."); }
    }

    private void printScoreboard() {
        System.out.println("\n╔══════════ FINAL SCOREBOARD ══════════╗");
        int[] scores = gameState.getScores();
        List<int[]> ranked = new ArrayList<>();
        for (int i = 0; i < scores.length; i++) ranked.add(new int[]{i, scores[i]});
        ranked.sort((a, b) -> Integer.compare(b[1], a[1]));
        for (int rank = 0; rank < ranked.size(); rank++) {
            int[] entry = ranked.get(rank);
            System.out.printf("║  %d. Player %-2d  →  %d points%n", rank + 1, entry[0] + 1, entry[1]);
        }
        System.out.println("╚══════════════════════════════════════╝");
    }

    public static void main(String[] args) throws IOException {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 5555;
        new GameServer(port).start();
    }
}
