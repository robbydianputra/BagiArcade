# Arcade by Bagicode 🕹️

**Arcade by Bagicode** is a collection of classic local multiplayer (PvP) games built with modern Android development practices. Designed for two players on a single device, it features a unique "Mirror Mode" for a seamless face-to-face gaming experience.

## 🚀 Features

- **Multi-Game Hub**: Easily navigate between different games using a clean, RecyclerView-based Home menu.
- **Mirror Mode**: Flip the UI and pieces for the opponent so two players can play comfortably while facing each other.
- **Adjustable Game Timers**: Choose between 5, 10, or 15-minute durations, or disable the timer entirely.
- **Move Predictions**: Visual guides for valid moves (available in Chess).
- **Pro Rules**: Fully implemented game rules including Castling, Pawn Promotion, and Checkmate/Stalemate detection.
- **Safety First**: Confirmation dialogs when exiting active games to prevent accidental progress loss.
- **Modern UI**: Built using Material Design components and custom Canvas-drawn views for high performance.

## 🎮 Current Games

### 1. Chess ♟️
A full-featured Chess implementation with:
- Standard movement for all pieces.
- High-quality vector assets (Cburnett style).
- Castling and Pawn Promotion (Queen, Rook, Bishop, Knight).
- King safety and checkmate detection.

### 2. Tic-Tac-Toe ❌⭕
The classic 3x3 grid game:
- Quick and snappy gameplay.
- Visual indicators for wins and draws.
- Colored symbols (Red X and Blue O) for clarity.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Platform**: Android (minSdk 24, targetSdk 37)
- **Architecture**: Activity-based with Custom Views for game boards.
- **Build System**: Gradle (Kotlin DSL)
- **Dependency**: Material Components, Activity-KTX, Core-KTX.

## 📦 Project Structure

- `com.bagicode.games.chess`: Contains all Chess logic, models, and activities.
- `com.bagicode.games.tictactoe`: Contains all Tic-Tac-Toe specific logic.
- `com.bagicode.games.HomeActivity`: The main entry point and game selection menu.

## 🎨 Attributions
- Chess piece designs: **Colin M.L. Burnett** (CC BY-SA 3.0).

---
Developed with ❤️ by **Bagicode**
