# Scotland Yard Game - Core Implementation

This repository contains the core implementation of the **Scotland Yard** game, focusing on game logic, board setup, and move validation. This implementation provides the foundational elements for both manual and AI-driven gameplay. It includes models for game state management, move validation, and player actions. The repository is designed with scalability in mind, making it easy to integrate custom AI strategies for MrX or the detectives.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Key Concepts](#key-concepts)
  - [Board and Game Setup](#board-and-game-setup)
  - [Player Moves](#player-moves)
  - [Game Logging](#game-logging)
  - [Model Factory](#model-factory)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running the Game](#running-the-game)
- [Technologies Used](#technologies-used)

---

## Project Overview

The Scotland Yard game is a detective-themed turn-based strategy game. This repository contains the **basic implementation of the game mechanics**, including:
- The setup of the game board.
- Management of player moves (both for MrX and the detectives).
- A logging system for tracking moves.
- Models for creating and managing the game state.

This implementation is modular and can be extended to include AI agents or other enhancements to the gameplay.

## Features

- **Game State Management**: The repository provides tools for managing and validating the current state of the game, including checking moves and updating the board.
- **Player Interaction**: Supports the core actions for MrX and the detectives, ensuring compliance with the game’s rules.
- **Logging**: Tracks all player moves, providing detailed logs that can be used for game replays or debugging.
- **AI Interface**: Although this is the basic implementation, it includes the structure to integrate AI strategies for player automation.

## Key Concepts

### Board and Game Setup
The board is modeled using `Board.java` and `ImmutableBoard.java`, which define the core structure of the game board and make it immutable for safer handling of game state. The `GameSetup.java` file is responsible for initializing the board with transport options and configuring the game rules.

### Player Moves
Moves are handled in the `Move.java` file, which defines both single and double moves, ensuring compliance with the game’s mechanics. The factory method provided in `MyModelFactory.java` manages the instantiation and validation of moves.

### Game Logging
Move history and game progression are tracked using `LogEntry.java`, which logs moves for MrX and the detectives. This allows for replaying games or auditing actions for correctness.

### Model Factory
The `MyModelFactory.java` is responsible for building the game model, including setting up the board, initializing the game, and managing observers. It also provides methods to retrieve the current game state and allow player interaction.

## Getting Started

### Prerequisites

To run the game, ensure you have the following installed:

- **Java 8+**: The game is developed in Java, so a Java Development Kit (JDK) is required.
- **Maven**: This project uses Maven for dependency management and building.

### Running the Game

1. Clone the repository:
    ```bash
    git clone https://github.com/yourusername/scotland-yard-game.git
    cd scotland-yard-game
    ```

2. Build the project:
    ```bash
    ./mvnw clean install
    ```

3. Run the game:
    ```bash
    java -cp target/your-built-jar-file.jar uk.ac.bris.cs.scotlandyard.Main
    ```

## Technologies Used

- **Java**: The primary language for the game implementation.
- **Maven**: Used for building and managing project dependencies.
- **Guava**: Utilized for immutable collections and graph structures.
- **Fugue (Atlassian)**: Provides functional programming utilities.

This core implementation showcases essential skills in game development, object-oriented design, and Java programming. It provides a flexible foundation that can be extended to support AI, multiplayer, or other game modes.
