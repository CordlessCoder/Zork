package org.example;

public class CLI {
    static void main(String[] args) {
        var terminal_view_controller = new TerminalViewController();
        GameState state = null;
        terminal_view_controller.presentMessage("Welcome to Zork, pick a save file or create a new one");
        var save_names = SaveManager.listSaveNames();
        var selected = terminal_view_controller.presentTextSelectionListWithPrompt(save_names, "enter the name of a save file to create");
        // Intentionally compares pointers to check if the user selected one of the options in the list, or entered a new save name.
        for (var save_file_ref : save_names) {
            if (save_file_ref != selected) {
                continue;
            }
            state = SaveManager.loadState(selected);
            break;
        }
        if (state == null) {
            // Recovering from an error where the initial_state.json, an internal file is missing, is impossible
            state = SaveManager.loadInitialState(selected).get();
        }
        ZorkInstance instance = new ZorkInstance(state);
        while (!terminal_view_controller.WasExitRequested()) {
            terminal_view_controller.presentTextPrompt("Please enter the action you want to perform:");
            var line = terminal_view_controller.consumeTextInput();
            if (line.isEmpty()) {
                return;
            }
            instance.advanceGame(terminal_view_controller, line.get());
        }
//        game.play();
    }
}
