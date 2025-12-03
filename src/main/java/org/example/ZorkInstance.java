package org.example;

public class ZorkInstance {
    public GameState state;

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
