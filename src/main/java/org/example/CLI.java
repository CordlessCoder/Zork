package org.example;

public class CLI {
    static void main(String[] args) {
        var terminal_view_controller = new TerminalViewController();
        var maybe_instance =  ZorkInstance.loadOrCreateNew(terminal_view_controller);
        if (maybe_instance.isEmpty()) {
            return;
        }
        ZorkInstance instance = maybe_instance.get();
        while (!terminal_view_controller.WasExitRequested()) {
            terminal_view_controller.presentTextPrompt("Please enter the action you want to perform:");
            var line = terminal_view_controller.consumeTextInput();
            if (line.isEmpty()) {
                return;
            }
            instance.advanceGame(terminal_view_controller, line.get());
        }
    }
}
