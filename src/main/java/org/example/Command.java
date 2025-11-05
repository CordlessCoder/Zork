package org.example;

import java.util.Optional;

public abstract class Command {
    abstract void execute(ZorkUL state);
}


class TakeItemCommandParser implements CommandParser {
    private static final RegexCommandHelper<Command> matcher = new RegexCommandHelper<>("^(?:take|pick up)", "^(?:take|pick up)(?: the)? ([a-zA-Z_]+)$", "Take what?", match -> {
        var item = match.group(1);
        return Optional.of(new Command() {
            @Override
            void execute(ZorkUL state) {
                state.takeItem(item);
            }
        });
    });

    @Override
    public Optional<Command> parse(String text) {
        return matcher.apply(text);
    }

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public String getDescription() {
        return "Pick up an item";
    }
}

class DropItemCommandParser implements CommandParser {
    private static final RegexCommandHelper<Command> matcher = new RegexCommandHelper<>("^drop", "^drop(?: the)? ([a-zA-Z_]+)$", "Drop what?", match -> {
        var item = match.group(1);
        return Optional.of(new Command() {
            @Override
            void execute(ZorkUL state) {
                state.dropItem(item);
            }
        });
    });

    @Override
    public Optional<Command> parse(String text) {
        return matcher.apply(text);
    }

    @Override
    public String getName() {
        return "drop";
    }

    @Override
    public String getDescription() {
        return "Drop an item";
    }
}

class GoCommandParser implements CommandParser {
    private static final RegexCommandHelper<Command> matcher = new RegexCommandHelper<>("^(?:go|move)", "^(?:go|move)(?: to)?(?: the)? (.+)$", "Go where?", match -> {
        var place = match.group(1);
        return Optional.of(new Command() {
            @Override
            void execute(ZorkUL state) {
                state.goTo(place);
            }
        });
    });

    @Override
    public Optional<Command> parse(String text) {
        return matcher.apply(text);
    }

    @Override
    public String getName() {
        return "go";
    }

    @Override
    public String getDescription() {
        return "Go through an exit";
    }
}

class LookCommandParser implements CommandParser {
    @Override
    public Optional<Command> parse(String text) {
        if (!text.equals("look")) {
            return Optional.empty();
        }
        return Optional.of(new Command() {
            @Override
            void execute(ZorkUL state) {
                state.lookMessage();
            }
        });
    }

    @Override
    public String getName() {
        return "look";
    }

    @Override
    public String getDescription() {
        return "Look around";
    }
}

class HelpCommandParser implements CommandParser {
    @Override
    public Optional<Command> parse(String text) {
        if (!text.equals("help")) {
            return Optional.empty();
        }
        return Optional.of(new Command() {
            @Override
            void execute(ZorkUL state) {
                state.printHelp();
            }
        });
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show this help message";
    }
}

class ExitCommandParser implements CommandParser {
    @Override
    public Optional<Command> parse(String text) {
        if (!text.equals("exit")) {
            return Optional.empty();
        }
        return Optional.of(new Command() {
            @Override
            void execute(ZorkUL state) {
                state.setExitRequested();
            }
        });
    }

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return "Exit the game";
    }
}
