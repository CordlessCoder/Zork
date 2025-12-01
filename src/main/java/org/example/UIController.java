package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UIController extends Application implements ViewController {
    private BlockingQueue<String> inputQueue;
    ZorkInstance instance;
    private volatile boolean exitRequested;
    private TextField commandPromptField;
    private TextArea outputArea;
    private Thread gameThread;

    static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // final BlockingQueue<String> _inputQueue = new LinkedBlockingQueue<>();
        this.inputQueue = new LinkedBlockingQueue<>();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("GameView.fxml")));
        Scene scene = new Scene(root);

        this.commandPromptField = (TextField) scene.lookup("#textPrompt");
        var executeButton = (Button) scene.lookup("#executeButton");
        this.outputArea = (TextArea) scene.lookup("#textView");
        this.outputArea.setEditable(false);
        this.outputArea.setWrapText(true);

        String[][] directions = {
                {"northButton", "go north"},
                {"southButton", "go south"},
                {"eastButton", "go east"},
                {"westButton", "go west"}
        };
        for (var direction : directions) {
            var button = (Button) scene.lookup('#' + direction[0]);
            button.setOnAction(event -> {
                inputQueue.offer(direction[1]);
            });
        }

        Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> suggestionProvider = request -> {
            if (this.instance == null) {
                return List.of();
            }
            return this.instance.state.autocomplete(request.getUserText());
        };
        TextFields.bindAutoCompletion(this.commandPromptField, suggestionProvider);

        stage.setScene(scene);
        stage.show();

        (executeButton).setOnAction(evt -> submitInputFromPrompt());
        this.commandPromptField.setOnAction(evt -> submitInputFromPrompt());

        this.gameThread = new Thread(() -> {
            try {
                GameState state = null;
                presentMessage("Welcome to Zork, pick a save file or create a new one");
                var save_names = SaveManager.listSaveNames();
                var selected = presentTextSelectionListWithPrompt(save_names, "enter the name of a save file to create");
                // Intentionally compares equality of strings to check if the user selected one of the options in the list, or entered a new save name.
                for (var save_file_ref : save_names) {
                    if (!save_file_ref.equals(selected)) {
                        continue;
                    }
                    state = SaveManager.loadState(selected);
                    break;
                }
                if (state == null) {
                    // Recovering from an error where the initial_state.json, an internal file is missing, is impossible
                    state = SaveManager.loadInitialState(selected).get();
                }
                this.instance = new ZorkInstance(state);
                while (!WasExitRequested()) {
                    presentTextPrompt("Please enter the action you want to perform:");
                    var line = consumeTextInput();
                    if (line.isEmpty()) {
                        return;
                    }
                    instance.advanceGame(this, line.get());
                }
            } catch (Exception e) {
                // ensure UI shows unexpected errors
                presentErrorMessage("Internal UI thread error: " + e.getMessage());
            }
        }, "Zork-GameThread");
        this.gameThread.start();
        stage.setOnCloseRequest(event -> notifyOfCompletion());
    }

    private void submitInputFromPrompt() {
        if (this.commandPromptField == null) return;
        var text = this.commandPromptField.getText();
        if (text == null) text = "";
        this.commandPromptField.clear();
        this.inputQueue.offer(text);
    }

    // helper to append to the output area (always call from background threads)
    private void appendOutput(String text) {
        Platform.runLater(() -> {
            outputArea.appendText(text);
            // Scroll to the bottom
            outputArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    @Override
    public boolean WasExitRequested() {
        return exitRequested;
    }

    @Override
    public void notifyOfCompletion() {
        exitRequested = true;
        // Unblock the game thread if it's waiting for input
        inputQueue.offer("");
        // Wake game thread if it's waiting on something else
        try {
            gameThread.interrupt();
        } catch (Exception ignored) {
        }
        Platform.runLater(() -> {
            try {
                Platform.exit();
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public <T> Optional<T> presentSelectionList(List<T> options) {
        if (options == null || options.isEmpty()) {
            presentMessage("(no options)");
            return Optional.empty();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < options.size(); i++) {
            sb.append((i + 1)).append(") ").append(options.get(i)).append("\n");
        }
        appendOutput(sb.toString());
        appendOutput("Choose an option (1-" + options.size() + "):\n");

        var line = consumeTextInput();
        if (line.isEmpty() || WasExitRequested()) {
            return Optional.empty();
        }
        var trimmed = line.get().trim();
        try {
            int idx = Integer.parseInt(trimmed);
            if (idx < 1 || idx > options.size()) {
                appendOutput("Invalid selection number.\n");
                return Optional.empty();
            }
            return Optional.of(options.get(idx - 1));
        } catch (NumberFormatException ignored) {
            appendOutput("Input was not a number.\n");
            return Optional.empty();
        }
    }

    @Override
    public String presentTextSelectionListWithPrompt(List<String> options, String prompt) {
        StringBuilder sb = new StringBuilder();
        if (options != null && !options.isEmpty()) {
            for (int i = 0; i < options.size(); i++) {
                sb.append((i + 1)).append(") ").append(options.get(i)).append("\n");
            }
            sb.append("\n");
        }
        sb.append(prompt);
        sb.append("\n> ");
        appendOutput(sb.toString());

        var line = consumeTextInput();
        if (line.isEmpty()) {
            return "";
        }
        var trimmed = line.get().trim();
        try {
            int idx = Integer.parseInt(trimmed);
            if (idx >= 1 && idx <= options.size()) {
                return options.get(idx - 1);
            }
        } catch (NumberFormatException ignored) {
        }
        return line.get();
    }

    @Override
    public Optional<String> consumeTextInput() {
        String line;
        try {
            line = inputQueue.take();
            System.err.println(line);
            appendOutput(line + "\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        if (WasExitRequested()) {
            return Optional.empty();
        }
        return Optional.ofNullable(line);
    }

    @Override
    public void presentTextPrompt(String prompt) {
        if (commandPromptField != null) {
            Platform.runLater(() -> {
                try {
                    commandPromptField.setPromptText(prompt);
                    commandPromptField.requestFocus();
                } catch (Exception ignored) {
                }
            });
        } else {
            appendOutput(prompt);
        }
    }

    @Override
    public void presentMessage(String message) {
        appendOutput(message + "\n");
    }

    @Override
    public void presentUrgentMessage(String message) {
        appendOutput("! " + message + "\n");
    }

    @Override
    public void presentErrorMessage(String message) {
        appendOutput("ERROR: " + message + "\n");
    }

}