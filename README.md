# WorldGeneration

**Build Your Own World (BYOW) — Procedurally Generated 2D Exploration Game**

## Overview

WorldGeneration is a Java-based 2D tile world exploration game built from scratch. The game procedurally generates a unique dungeon-style world on every run using a seed-based algorithm, allowing players to explore rooms and hallways, interact with the environment, and experience a surprise ending.

## Features

**Procedural World Generation**
The world is generated algorithmically from a user-provided seed, producing a unique layout of rooms and connecting hallways each time. The generation system places rooms randomly within the tile grid, resolves overlaps, and connects rooms with corridors to ensure a fully traversable map.

**Player Interactivity**
Players navigate the world using keyboard controls, moving through rooms and hallways in real time. The game tracks player position and handles collision detection with walls and boundaries.

**Save and Load**
Game state can be saved mid-session and restored, allowing players to continue exploration across multiple sessions.

**Jumpscare Ending**
The game includes a hidden trigger event that activates a jumpscare sequence when the player reaches a specific condition, serving as the game's climax.

## Tech Stack

Java, with the Princeton StdLib tile rendering engine (used in CS61B at UC Berkeley).

## How to Run

```bash
javac proj5/src/*.java
java proj5/src/Main
```

Enter a seed when prompted to generate your world. Use WASD or arrow keys to move. Press S to save, and L at the main menu to load a saved game.

## Authors

**Alex Liu** — world generation algorithm, player interactivity, jumpscare ending

**Adarsh** — save and load system
