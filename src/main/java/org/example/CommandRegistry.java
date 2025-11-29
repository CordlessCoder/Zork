package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// While working on this, I ran into multiple infuriating and senseless limitations:
// - abstract classes cannot have static methods implemented by subclasses
// - interfaces cannot have static methods implemented by implementors
//
// This is caused by Java not allowing static methods to be overridden -
// something I do quite often in other(better) languages.
// Java makes me deeply sad.

interface CommandParser {
    Optional<Command> parse(String text);

    void autoComplete(GameState context, ArrayList<String> output, String text);
    void registerDirectCompletions(CompletionTrie trie);

    String getName();

    String getDescription();
}

public class CommandRegistry {
    public final static CommandRegistry INSTANCE = new CommandRegistry();

    static {
        CommandRegistry.registerParser(new HelpCommandParser());
        CommandRegistry.registerParser(new LookCommandParser());
        CommandRegistry.registerParser(new MapCommandParser());
        CommandRegistry.registerParser(new TakeItemCommandParser());
        CommandRegistry.registerParser(new DropItemCommandParser());
        CommandRegistry.registerParser(new GoCommandParser());
        CommandRegistry.registerParser(new SaveCommandParser());
        CommandRegistry.registerParser(new ExitCommandParser());
    }

    private final ArrayList<CommandParser> commandParsers = new ArrayList<>();
    private final CompletionTrie completionTrie = new CompletionTrie();

    private CommandRegistry() {
    }

    public static String describeCommands() {
        var buffer = new StringBuilder();
        boolean first = true;
        for (final var cmd : INSTANCE.commandParsers) {
            if (!first) {
                buffer.append("\n");
            }
            first = false;

            var name = cmd.getName();
            var description = cmd.getDescription();
            buffer.append("- ");
            buffer.append(name);
            buffer.append(": ");
            buffer.append(description);
        }
        return buffer.toString();
    }

    private static void registerParser(CommandParser parser) {
        INSTANCE.commandParsers.add(parser);
        parser.registerDirectCompletions(INSTANCE.completionTrie);
    }

    public static List<String> autocomplete(GameState context, String text) {
        var results = INSTANCE.completionTrie.search(text);
        if (results.isEmpty()) {
            for (final var parser : INSTANCE.commandParsers) {
                parser.autoComplete(context, results, text);
            }
        }
        return results;
    }

    public static Optional<Command> parse(String text) {
        for (final var parser : INSTANCE.commandParsers) {
            var command = parser.parse(text);
            if (command.isEmpty()) {
                continue;
            }
            return command;
        }
        return Optional.empty();
    }
}
