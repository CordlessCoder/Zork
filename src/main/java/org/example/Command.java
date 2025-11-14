package org.example;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

public abstract class Command {
    abstract void execute(Box<GameState> state);
}


class TakeItemCommandParser implements CommandParser {
    private static final RegexCommandHelper<Command> matcher = new RegexCommandHelper<>("^(?:take|pick up|grab)", "^(?:take|pick up|grab)(?: the)? ([a-zA-Z_]+)$", "Take what?", match -> {
        var item = match.group(1);
        return Optional.of(new Command() {
            @Override
            void execute(Box<GameState> state) {
                state.inner.takeItem(item);
            }
        });
    });

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("take ", "pick up ", "grab ");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
        // TODO: Implement auto-complete for items in the room
    }

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
            void execute(Box<GameState> state) {
                state.inner.dropItem(item);
            }
        });
    });

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("drop ", "drop the ");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
        // TODO: Implement auto-complete for currently held items
    }

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
    private static final RegexCommandHelper<Command> matcher = new RegexCommandHelper<>("^(?:go|move)", "^(?:go|move)(?: to(?: the)?)? (.+)$", "Go where?", match -> {
        var place = match.group(1);
        return Optional.of(new Command() {
            @Override
            void execute(Box<GameState> state) {
                state.inner.goTo(place);
            }
        });
    });

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("go ", "move ", "go to ", "move to ", "go to the ", "move to the");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
        // TODO: Implement auto-complete for directions with exits
    }

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
            void execute(Box<GameState> state) {
                state.inner.lookMessage();
            }
        });
    }

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("look");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
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
            void execute(Box<GameState> state) {
                state.inner.showHelp();
            }
        });
    }

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("help");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
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
            void execute(Box<GameState> state) {
                state.inner.setExitRequested();
            }
        });
    }

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("exit");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
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

class SaveCommandParser implements CommandParser {
    private final static Pattern SAVE_AS_PATTERN = Pattern.compile("save as ([a-zA-Z1-9_\\-])");
    private final static Pattern LOAD_PATTERN = Pattern.compile("load ([a-zA-Z1-9_\\-])");

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("save", "save as ", "delete save", "load ");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
    }

    @Override
    public Optional<Command> parse(String text) {
        var save_as_matcher = SAVE_AS_PATTERN.matcher(text);
        if (save_as_matcher.matches()) {
            var name = save_as_matcher.group(1);
            return Optional.of(new Command() {
                @Override
                void execute(Box<GameState> state) {
                    SaveManager.saveState(name, state.inner);
                }
            });
        }
        var load_matcher = LOAD_PATTERN.matcher(text);
        if (load_matcher.matches()) {
            var name = load_matcher.group(1);
            return Optional.of(new Command() {
                @Override
                void execute(Box<GameState> state) {
                    var new_state = SaveManager.loadState(name);
                    state.replace(new_state);
                }
            });
        }
        return switch (text.trim()) {
            case "save as" -> Optional.of(new Command() {
                @Override
                void execute(Box<GameState> state) {
                    state.inner.controller.presentMessage("Pick a save file to overwrite, or enter the name of the save file to create");
                    var save_names = SaveManager.listSaveNames();
                    var selected = state.inner.controller.presentTextSelectionListWithPrompt(save_names, "Create new save");
                    SaveManager.saveState(selected, state.inner);
                }
            });
            case "load" -> Optional.of(new Command() {
                @Override
                void execute(Box<GameState> state) {
                    state.inner.controller.presentMessage("Pick a save file to load");
                    var save_names = SaveManager.listSaveNames();
                    var selected = state.inner.controller.presentSelectionList(save_names);
                    if (selected.isEmpty()) {
                        return;
                    }
                    var new_state = SaveManager.loadState(selected.get());
                    state.replace(new_state);
                }
            });
            case "delete save" -> Optional.of(new Command() {
                @Override
                void execute(Box<GameState> state) {
                    state.inner.controller.presentMessage("Pick a save file to delete");
                    var save_names = SaveManager.listSaveNames();
                    var selected = state.inner.controller.presentSelectionList(save_names);
                    if (selected.isEmpty()) {
                        return;
                    }

                    var save_file_path = SaveManager.pathForSaveName(selected.get());
                    var _ = save_file_path.toFile().delete();
                }
            });
            case "save" -> Optional.of(new Command() {
                @Override
                void execute(Box<GameState> state) {
                    SaveManager.saveState(state.inner.save_name, state.inner);
                }
            });
            default -> Optional.empty();
        };
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