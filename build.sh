#!/bin/bash
# ─────────────────────────────────────────────
#  Hangman Multiplayer - Build & Run Script
# ─────────────────────────────────────────────

echo "Compiling Hangman Multiplayer..."

# Create output dir
mkdir -p out

# Compile all source files
javac -d out \
  src/common/Protocol.java \
  src/common/GameState.java \
  src/server/ClientHandler.java \
  src/server/GameServer.java \
  src/client/GameClient.java

if [ $? -eq 0 ]; then
  echo "✅ Compilation successful!"
  echo ""
  echo "To run the SERVER (host):"
  echo "  java -cp out server.GameServer 5555"
  echo ""
  echo "To run a CLIENT (player - on another machine):"
  echo "  java -cp out client.GameClient <host-ip> 5555"
  echo ""
  echo "Example (same machine testing):"
  echo "  Terminal 1: java -cp out server.GameServer 5555"
  echo "  Terminal 2: java -cp out client.GameClient localhost 5555"
  echo "  Terminal 3: java -cp out client.GameClient localhost 5555"
  echo "  Terminal 4: java -cp out client.GameClient localhost 5555"
else
  echo "❌ Compilation failed. Check errors above."
fi
