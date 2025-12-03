package org.example;

import java.util.Optional;

public class ZorkInstance {
    public GameState state;

    public static Optional<ZorkInstance> loadOrCreateNew(ViewController controller) {
        GameState state = null;
        controller.presentMessage("Welcome to Zork, pick a save file or create a new one");
        var save_names = SaveManager.listSaveNames();
        var selected = controller.presentTextSelectionListWithPrompt(save_names, "enter the name of a save file to create");
        // Intentionally compares pointers to check if the user selected one of the options in the list, or entered a new save name.
        for (var save_file_ref : save_names) {
            if (save_file_ref != selected) {
                continue;
            }
            state = SaveManager.loadState(selected);
            break;
        }
        if (state == null) {
            var initial_state = SaveManager.loadInitialState(selected);
            if (initial_state.isEmpty()) {
                controller.presentErrorMessage("Internal file `initial_state.json` is missing.");
                return Optional.empty();
            }
            state = initial_state.get();
        }
        return Optional.of(new ZorkInstance(state));
    }

    public ZorkInstance(GameState state) {
        this.state = state;
    }

    public void advanceGame(ViewController controller, String command) {
        this.state.controller = controller;
        if (controller.WasExitRequested()) {
            this.state.isExitRequested = true;
            return;
        }
        var cmd = CommandRegistry.parse(command);
        if (cmd.isEmpty()) {
            var suggestions = CommandRegistry.autocomplete(this.state, command);
            if (suggestions.isEmpty()) {
                controller.presentUrgentMessage("Unknown command.");
            } else {
                controller.presentUrgentMessage("Did you mean any of: " + String.join(", ", suggestions.stream().map(String::trim).filter(suggestion -> !suggestion.equals(command)).toList()) + "?");
            }
            return;
        }
        try {
            cmd.get().execute(this);
        } catch (CommandException e) {
            controller.presentErrorMessage("Command failed: " + e.getMessage());
        }
        if (this.state.isExitRequested) {
            controller.notifyOfCompletion();
        }
    }

    @Override
    public String toString() {
        return "ZorkInstance{" +
                "state=" + state +
                '}';
    }
}
