# 🎮 Hangman (Java Terminal Game)

A simple yet fun **Hangman game built in Java**, playable directly in the terminal.
Guess the word letter-by-letter or try to solve it in one shot — but be careful, you only get **6 chances**!

---

## 🚀 Features

* 🎯 Random word selection from a file (`Words.txt`)
* 🔤 Guess letters or the full word
* 💀 ASCII-based hangman visualization
* 📉 Limited chances (6 lives)
* 📜 Clean terminal interaction
* ⚡ Lightweight and fast (no external libraries)

---

## 🛠️ Tech Stack

* **Java (JDK 21)**
* File Handling (`BufferedReader`, `FileReader`)
* Collections (`ArrayList`)
* Randomization (`Random`)
* User Input (`Scanner`)

---

## 📂 Project Structure

```
Hangman/
│── src/
│   ├── Main.java      # Game logic
│   ├── Words.txt      # Word list
│
│── .gitignore
│── Hangman.iml
```

---

## ▶️ How to Run

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/hangman-java.git
cd hangman-java
```

### 2. Compile the Program

```bash
javac src/Main.java
```

### 3. Run the Game

```bash
java -cp src Main
```

---

## 🎮 Gameplay

```
******************
Welcome to Hangman
******************

You have 6 Chances to guess the word
The word has X letters
_____

Choose your action:
1. Guess a letter
2. Guess the word
```

* ✔ Correct guess → Reveals letters
* ❌ Wrong guess → Builds the hangman
* 🏆 Guess all letters → **YOU WIN!**
* ☠️ Run out of chances → **YOU LOSE!**

---

## 🧠 How It Works

* Reads words from `Words.txt`
* Picks a random word
* Tracks:

  * Guessed letters
  * Remaining chances
* Updates UI after each move
* Ends when:

  * Word is guessed OR
  * Chances reach 0

---

## 📸 Sample Hangman Stages

```
firstWrongGuess
                -------------------------
                            |
                            |
                            |
                            O
secondWrongGuess
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
thirdWrongGuess
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

fourthWrongGuess
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

fifthWrongGuess
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

sixthWrongGuess
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
```

---

## ⚙️ Customization

You can easily modify:

* 📄 `Words.txt` → Add your own words
* ❤️ Number of lives → Change `chances = 6`
* 🎨 ASCII art → Improve visuals
* 🧩 Game logic → Add difficulty levels, hints, etc.

---

## 💡 Future Improvements

* 🎯 Difficulty modes (Easy/Medium/Hard)
* ⏱ Timer-based gameplay
* 🧍 Multiplayer mode
* 🌐 GUI version (Swing / JavaFX / Web)
* 📊 Score tracking

---

## 🤝 Contributing

Feel free to fork this repo and improve it!

```bash
git checkout -b feature/your-feature-name
```

---

## 📜 License

This project is open-source and free to use.

---

## 👨‍💻 Author

Built with 💻 + ☕ + a bit of chaos 😄

---

## ⭐ Show Some Love

If you liked this project:

* ⭐ Star the repo
* 🍴 Fork it
* 🧠 Improve it

---

> "Every wrong guess gets you closer to the truth." 🎯
