package org.example;

public class ZorkInstance {
    public Box<GameState> state;

    public ZorkInstance(GameState state) {
        this.state = new Box<>(state);
    }

    public void advanceGame(ViewController controller, String command) {
        this.state.inner.controller = controller;
        if (controller.WasExitRequested()) {
            this.state.inner.isExitRequested = true;
            return;
        }
        var cmd = CommandRegistry.parse(command);
        if (cmd.isEmpty()) {
            controller.presentUrgentMessage("Unknown command.");
            controller.presentErrorMessage(CommandRegistry.autocomplete(this.state.inner, command).toString());
            return;
        }
        cmd.get().execute(this.state);
        if (this.state.inner.isExitRequested) {
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
