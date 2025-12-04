# Java Text Adventure
A simple Java Text adventure created for the CS4431 module.

This implementation is unique in that the game logic is entirely UI-agnostic - the default interface is a JavaFX GUI,
but the game can be launched in the terminal by passing `--cli` as a command line argument.

# Building
```bash
mvn package
```
# Running
```bash
java -jar ./target/Zork-1.0-SNAPSHOT.jar
```

# Notable features
As part of this assignment, I built a custom 2D array type `Matrix<T>`, a custom double-ended concurrent queue `BlockingArrayListDeque<T>`,
and a trie(`CompletionTrie`).

The 2D array is used for building the map layout used to provide the `map` command.

The queue is used for communication between the UI and game threads.

The trie is used to provide efficient autocompletion for fixed sets of strings(top-level commands).

# Saving
Saving is handled via serialization and deserialization using the jackson library, through the `GameState` class.

# Java language feature usage
Generics are used for the aforementioned data structure implementations.

Enums are used to express the finite states of certain items, and the possible directions
that the user can move in - allowing the use of an `EnumMap`, making exit lookups simple array indexing operations
instead of hashmap lookup.

Commands implement the `Command` interface, and are parsed by implementers of the `CommandParser` interface.

All command parsers are registered in the `CommandRegistry` singleton, which handles coalescing all the command's parsers and autocompletion
into one API.

Inheritance is used by Rooms and Items to provide custom behavior on room entry, item usage etc.

The UI and game run in separate threads, ensuring the UI remains responsive.

Commands use a mix of regex patterns and manual parsing to provide maximal flexiblity with good performance.

Commands can raise the `CommandException` exception to signal command failure.
