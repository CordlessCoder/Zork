package org.example;

import tools.jackson.core.JacksonException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command {
    abstract void execute(ZorkInstance instance) throws CommandException;
}

class ItemAutocompleteHelper {
    public static void autoCompleteItemsInRoom(GameState context, ArrayList<String> output, String text, String before_name) {
        var room = context.getCurrentRoom();
        text = text.toLowerCase();
        for (var item : room.items) {
            var name = item.toLowerCase();
            if (!name.startsWith(text)) {
                continue;
            }
            if (name.length() == text.length()) {
                continue;
            }
            output.add(before_name + name);
        }
    }

    public static void autoCompleteHeldItems(GameState context, ArrayList<String> output, String text, String before_name) {
        text = text.toLowerCase();
        for (var item : context.player.items) {
            var name = item.toLowerCase();
            if (!name.startsWith(text)) {
                continue;
            }
            if (name.length() == text.length()) {
                continue;
            }
            output.add(before_name + name);
        }
    }
}


class TakeItemCommandParser implements CommandParser {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(?:take|pick up|grab)(?: the)? ([a-zA-Z_]*)$");
    private static final RegexCommandHelper<Command> matcher = new RegexCommandHelper<>("^(?:take|pick up|grab)", COMMAND_PATTERN, "Take what?", match -> {
        var item = match.group(1);
        return Optional.of(new Command() {
            @Override
            void execute(ZorkInstance instance) {
                instance.state.takeItem(item);
            }
        });
    });

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("take", "pick up", "grab");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
        var matcher = COMMAND_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return;
        }
        var before_name = text.substring(0, matcher.start(1));
        var item_name = matcher.group(1);
        ItemAutocompleteHelper.autoCompleteItemsInRoom(context, output, item_name, before_name);
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
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^drop(?: the)? ([a-zA-Z_]*)$");
    private static final RegexCommandHelper<Command> matcher = new RegexCommandHelper<>("^drop", COMMAND_PATTERN, "Drop what?", match -> {
        var item = match.group(1);
        return Optional.of(new Command() {
            @Override
            void execute(ZorkInstance instance) {
                instance.state.dropItem(item);
            }
        });
    });

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("drop");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
        var matcher = COMMAND_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return;
        }
        var before_name = text.substring(0, matcher.start(1));
        var item_name = matcher.group(1);
        ItemAutocompleteHelper.autoCompleteHeldItems(context, output, item_name, before_name);
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
    private static final Pattern COMMAND_PATTERN  = Pattern.compile("^(?:go|move)(?: to(?: the)?)? (.*)$");
    private static final RegexCommandHelper<Command> matcher = new RegexCommandHelper<>("^(?:go|move)", COMMAND_PATTERN, "Go where?", match -> {
        var place = match.group(1);
        return Optional.of(new Command() {
            @Override
            void execute(ZorkInstance instance) {
                instance.state.goTo(place);
            }
        });
    });

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("go", "move");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
        var matcher = COMMAND_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return;
        }
        var before_name = text.substring(0, matcher.start(1));
        var exit_name = matcher.group(1);
        var room = context.getCurrentRoom();
        for (var exit : room.paths.keySet()) {
            var name = exit.toString().toLowerCase();
            if (!name.startsWith(exit_name.toLowerCase())) {
                continue;
            }
            if (name.length() == exit_name.length()) {
                continue;
            }
            output.add(before_name + name);
        }
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
            void execute(ZorkInstance instance) {
                instance.state.lookMessage();
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

class MapCommandParser implements CommandParser {
    @Override
    public Optional<Command> parse(String text) {
        if (!text.equals("map")) {
            return Optional.empty();
        }
        return Optional.of(new Command() {
            @Override
            void execute(ZorkInstance instance) {
                instance.state.mapMessage();
            }
        });
    }

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("map");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
    }

    @Override
    public String getName() {
        return "map";
    }

    @Override
    public String getDescription() {
        return "Show the map";
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
            void execute(ZorkInstance instance) {
                instance.state.showHelp();
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
            void execute(ZorkInstance instance) {
                instance.state.setExitRequested();
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
    private final static Pattern SAVE_AS_PATTERN = Pattern.compile("^save as ([a-zA-Z1-9_\\-]*)$");
    private final static Pattern LOAD_PATTERN = Pattern.compile("^load ([a-zA-Z1-9_\\-]*)$");
    private final static Pattern DELETE_SAVE_PATTERN = Pattern.compile("^delete save ([a-zA-Z1-9_\\-]*)$");

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("save", "save as", "delete save", "load");
    }

    private void autoCompleteSaveNames(ArrayList<String> output, String text, Matcher matcher) {
        var save_names = SaveManager.listSaveNames();
        var before_name = text.substring(0, matcher.start(1));
        var name = matcher.group(1);
        for (var save_name : save_names) {
            if (!save_name.startsWith(name)) {
                continue;
            }
            if (save_name.length() == name.length()) {
                continue;
            }
            output.add(before_name + save_name);
        }
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
        var matcher = LOAD_PATTERN.matcher(text);
        if (matcher.matches()) {
            autoCompleteSaveNames(output, text, matcher);
        }
        matcher = DELETE_SAVE_PATTERN.matcher(text);
        if (matcher.matches()) {
            autoCompleteSaveNames(output, text, matcher);
        }
    }

    @Override
    public Optional<Command> parse(String text) {
        var save_as_matcher = SAVE_AS_PATTERN.matcher(text);
        if (save_as_matcher.matches()) {
            var name = save_as_matcher.group(1);
            return Optional.of(new Command() {
                @Override
                void execute(ZorkInstance instance) {
                    SaveManager.saveState(name, instance.state);
                }
            });
        }
        var load_matcher = LOAD_PATTERN.matcher(text);
        if (load_matcher.matches()) {
            var name = load_matcher.group(1);
            return Optional.of(new Command() {
                @Override
                void execute(ZorkInstance instance) throws CommandException {
                    try {
                        instance.state = SaveManager.loadState(name);
                    } catch (JacksonException e) {
                        throw new CommandException("Failed to load save file: " + name);
                    }
                }
            });
        }
        var delete_matcher = DELETE_SAVE_PATTERN.matcher(text);
        if (delete_matcher.matches()) {
            var name = delete_matcher.group(1);
            return Optional.of(new Command() {
                @Override
                void execute(ZorkInstance instance) {
                    var save_file_path = SaveManager.pathForSaveName(name);
                    var _ = save_file_path.toFile().delete();
                }
            });
        }
        return switch (text.trim()) {
            case "save as" -> Optional.of(new Command() {
                @Override
                void execute(ZorkInstance instance) {
                    instance.state.controller.presentMessage("Pick a save file to overwrite, or enter the name of the save file to create");
                    var save_names = SaveManager.listSaveNames();
                    var selected = instance.state.controller.presentTextSelectionListWithPrompt(save_names, "Create new save");
                    SaveManager.saveState(selected, instance.state);
                }
            });
            case "load" -> Optional.of(new Command() {
                @Override
                void execute(ZorkInstance instance) {
                    instance.state.controller.presentMessage("Pick a save file to load");
                    var save_names = SaveManager.listSaveNames();
                    var selected = instance.state.controller.presentSelectionList(save_names);
                    if (selected.isEmpty()) {
                        return;
                    }
                    instance.state = SaveManager.loadState(selected.get());
                }
            });
            case "delete save" -> Optional.of(new Command() {
                @Override
                void execute(ZorkInstance instance) {
                    instance.state.controller.presentMessage("Pick a save file to delete");
                    var save_names = SaveManager.listSaveNames();
                    var selected = instance.state.controller.presentSelectionList(save_names);
                    if (selected.isEmpty()) {
                        return;
                    }

                    var save_file_path = SaveManager.pathForSaveName(selected.get());
                    var _ = save_file_path.toFile().delete();
                }
            });
            case "save" -> Optional.of(new Command() {
                @Override
                void execute(ZorkInstance instance) {
                    SaveManager.saveState(instance.state.save_name, instance.state);
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
class UseItemCommandParser implements CommandParser {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^use(?: the)? ([a-zA-Z_]*)$");
    private static final RegexCommandHelper<Command> matcher = new RegexCommandHelper<>("^use", COMMAND_PATTERN, "Use what?", match -> {
        var item = match.group(1);
        return Optional.of(new Command() {
            @Override
            void execute(ZorkInstance instance) {
                instance.state.useItem(item);
            }
        });
    });

    @Override
    public void registerDirectCompletions(CompletionTrie trie) {
        trie.insertAll("use");
    }

    @Override
    public void autoComplete(GameState context, ArrayList<String> output, String text) {
        var matcher = COMMAND_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return;
        }
        var before_name = text.substring(0, matcher.start(1));
        var item_name = matcher.group(1);
        ItemAutocompleteHelper.autoCompleteHeldItems(context, output, item_name, before_name);
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