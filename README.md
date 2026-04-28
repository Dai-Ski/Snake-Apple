# 🐍 Snake & Apple

A modern, highly-polished, and minimal desktop Snake engine built from scratch in Java.

![Gameplay](assets/gameplay_screenshot.png)

## ✨ Features
- **Clean Architecture:** Extremely minimal and consolidated codebase (only 5 core files!) ensuring zero logic leakage into the UI.
- **Fluid & Responsive:** Includes an intelligent 2-move input buffer to completely eliminate rapid key-mashing bugs.
- **Dynamic Visuals:** Modern dark-mode checkered grid, directional snake eyes, and organic apple shapes rendered cleanly via JavaFX.
- **Pause & Resume:** Built-in spacebar toggle to pause the action instantly with a translucent overlay.
- **Seamless Wrap-Around:** Wall-collisions seamlessly teleport you to the opposite side for continuous play.

## 🎮 Controls
- **Movement:** `W` `A` `S` `D`  *or*  `Arrow Keys`
- **Pause / Play:** `Spacebar`

## 🚀 How to Run

Ensure you have **Java 17+** and **Maven** installed on your system.

1. Clone this repository:
   ```bash
   git clone https://github.com/Dai-Ski/Snake-Apple.git
   cd Snake-Apple
   ```
2. Compile and Run:
   ```bash
   mvn clean compile javafx:run
   ```

## 🛠️ Architecture Overview
The entire application has been aggressively refactored for maximum readability and ease of explanation.
*   `GameState.java`: Immutable data models and pure state snapshots.
*   `Game.java`: The core deterministic engine and ruleset.
*   `Snake.java`: Manages growth logic and spatial positioning.
*   `GameRenderer.java`: Clean JavaFX presentation layer.
*   `Main.java`: Input buffering and fixed 8-tick engine loop.

---

*Built with precision and ❤️ using Java & Clean Architecture principles.*
