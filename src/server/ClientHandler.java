package server;

import common.Protocol;

import java.io.*;
import java.net.Socket;

/**
 * Runs on the server in its own thread, one per connected player.
 * Reads messages from the client and forwards guesses to GameServer.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final GameServer server;
    private final int playerIndex;
    private PrintWriter out;
    private static int counter = 0;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        this.playerIndex = counter++;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = writer;

            // Welcome the player before game starts
            send(Protocol.welcome(playerIndex));

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(Protocol.GUESS_PREFIX)) {
                    String guess = line.substring(Protocol.GUESS_PREFIX.length()).trim().toLowerCase();
                    if (!guess.isEmpty()) {
                        server.processGuess(playerIndex, guess);
                    }
                } else if (line.equals(Protocol.PING)) {
                    send(Protocol.PONG);
                }
            }
        } catch (IOException e) {
            System.out.println("Player " + (playerIndex + 1) + " disconnected: " + e.getMessage());
        } finally {
            server.removeClient(this);
            // Notify the game loop so it doesn't hang waiting
            synchronized (server.getTurnLock()) {
                server.getTurnLock().notifyAll();
            }
        }
    }

    public void send(String message) {
        if (out != null) out.println(message);
    }
}
