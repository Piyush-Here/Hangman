# 🎮 Hangman Multiplayer (Java Terminal) — v1

A turn-based multiplayer Hangman built on top of the original single-player version.
Players join over **LAN, Hotspot, Bluetooth-bridged network, or the internet**.
The host machine runs the **Server**; every player runs the **Client**.

---

## 📁 Project Structure

```
HangmanMultiplayer/
├── src/
│   ├── common/
│   │   ├── GameState.java       ← All game logic (word, guesses, scores, options)
│   │   └── Protocol.java        ← Text-based message format (client ↔ server)
│   ├── server/
│   │   ├── GameServer.java      ← Host runs this
│   │   └── ClientHandler.java   ← One per connected player (threaded)
│   └── client/
│       └── GameClient.java      ← Players run this
├── build.sh                     ← Compile everything
└── README.md
```

---

## 🚀 Quick Start

### 1. Compile
```bash
chmod +x build.sh
./build.sh
```

Or manually:
```bash
mkdir -p out
javac -d out src/common/Protocol.java src/common/GameState.java \
             src/server/ClientHandler.java src/server/GameServer.java \
             src/client/GameClient.java
```

### 2. Host starts the server
```bash
java -cp out server.GameServer 5555
```
The server will print your **LAN IP** (e.g. `192.168.1.10`). Share this with players.

### 3. Each player connects
```bash
java -cp out client.GameClient 192.168.1.10 5555
```

### 4. Host starts the game
Once ≥ 3 players are connected, the host types `START` in their terminal,
chooses Easy/Hard mode, and optionally provides a custom word file path.

---

## 🎮 Game Rules Implemented

| Rule | Detail |
|------|--------|
| **Min 3 players** | Server refuses to start with fewer than 3 |
| **N players → ≤ N-1 options (max 26)** | `GameState.generateOptionsForTurn()` |
| **At least 1 correct option** | Guaranteed before shuffling |
| **Easy mode: wrong options hidden** | Already-wrong letters excluded from pool |
| **Hard mode: wrong options reappear** | Full alphabet pool used every turn |
| **Easy mode: word length ≤ 6** | Words filtered when loading |
| **Hard mode: word length ≥ 7** | Words filtered when loading |
| **Custom word set (≥ 1000 words)** | Host provides `.txt` path at startup |
| **Scoring** | +10 per revealed letter, +50 for full word, −5/−10 wrong guess penalty |
| **Turn-based** | Players take turns in join order; 60s timeout per turn |

---

## 📡 Networking Options

See the "Tech to Learn" section at the bottom for details on each approach.

| Method | How |
|--------|-----|
| **Same machine** | Use `localhost` as the IP |
| **Wired LAN** | Share the LAN IP printed by the server |
| **Wi-Fi / Hotspot** | Host creates a hotspot; players join it; share the IP |
| **Bluetooth** | Use Bluetooth PAN (network tethering) — bridge to LAN, same IP approach |
| **Internet** | Port-forward 5555 on router OR use an SSH tunnel / ngrok |

---

## 🗂 Custom Word File Format

```
abandon
achieve
balance
...
```
Plain `.txt` with one word per line, only lowercase a–z letters.
Must contain **≥ 1000 valid words** (matching the chosen mode's length filter), otherwise the default set is used.

---

## 🛠️ Tech Stack

- **Java 21** (text blocks, records-friendly, modern switch)
- `java.net.ServerSocket` / `Socket` — TCP networking
- `java.io.PrintWriter` / `BufferedReader` — line-delimited protocol
- `Thread` per client — simple thread-per-connection model
- No external libraries needed

---

## 🧠 Tech You Need to Learn for Real LAN/Network Multiplayer

### Core (you already have the foundation for these)

**1. Java Sockets (TCP)**
- `ServerSocket`, `Socket`, `InputStream`/`OutputStream`
- Already used in this project — go deeper with:
  - [Oracle Java Networking Tutorial](https://docs.oracle.com/javase/tutorial/networking/sockets/)

**2. Multithreading**
- `Thread`, `Runnable`, `synchronized`, `wait()`/`notify()`
- `ExecutorService` and thread pools for scalability
- Used in this project — topics to study: race conditions, deadlocks

**3. I/O Streams**
- `BufferedReader`, `PrintWriter`, `InputStreamReader`
- Understand blocking I/O vs non-blocking (NIO)

### Networking Concepts

**4. IP Addresses & Ports**
- IPv4 vs IPv6, private vs public IP
- What port numbers are (0–65535), well-known vs ephemeral ports
- How to find your LAN IP: `ipconfig` (Windows) / `ifconfig` or `ip a` (Linux/Mac)

**5. NAT & Port Forwarding (for internet play)**
- Why your device behind a router isn't directly reachable
- How to set up port forwarding on your router (access `192.168.1.1`)
- Tools: ngrok (`ngrok tcp 5555`) or Tailscale for zero-config tunneling

**6. Firewall Rules**
- How to allow inbound connections on a port
- Windows Defender Firewall / `ufw` on Linux

**7. Hotspot Networking**
- When you create a mobile or PC hotspot, the host gets an IP like `192.168.43.1`
- All devices on the hotspot are on the same subnet — direct TCP works

**8. Bluetooth PAN (Personal Area Network)**
- Bluetooth isn't a raw network — you use Bluetooth PAN to bridge it to TCP/IP
- On Linux: `bluetoothctl`, `bt-pan`, then treat it like a LAN
- On Windows: Pair → connect as "Network Access Point"
- Once PAN is active, the device gets an IP and the rest is standard TCP

### Optional but Powerful

**9. NIO (Non-blocking I/O) — for scaling to many players**
- `java.nio.channels.Selector`, `ServerSocketChannel`, `SocketChannel`
- Lets one thread handle many connections instead of one thread per player

**10. Protocol Design**
- How to define clear message formats (what this project already does with `Protocol.java`)
- Consider JSON (via Gson/Jackson) or Protocol Buffers for more complex games

**11. SSH Tunneling (for internet without port forwarding)**
```bash
# On a VPS:
ssh -R 5555:localhost:5555 user@your-vps-ip
# Players connect to your-vps-ip:5555
```

**12. Ngrok (easiest way to expose local server)**
```bash
ngrok tcp 5555
# Gives you something like: tcp://0.tcp.ngrok.io:12345
# Share that address with players
```

---

## 🔮 Future Versions (v2 ideas)

- [ ] Spectator mode for late joiners
- [ ] Rematch / multiple rounds with cumulative score
- [ ] NIO-based server (handle 50+ players)
- [ ] GUI client (JavaFX or web-based via WebSockets)
- [ ] Player name registration
- [ ] Chat between players
- [ ] Reconnection support

---

> Built with ☕ Java + 🔌 TCP sockets + a healthy dose of competitive spelling
