package org.example;

import javafx.application.Application;
import javafx.application.Platform;
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
import java.util.*;
import java.util.stream.IntStream;

public class UIController extends Application implements ViewController {
    private final EnumMap<Direction, Button> directionButtons = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, Boolean> savedButtonState = new EnumMap<>(Direction.class);
    volatile ZorkInstance instance;
    private BlockingArrayListDeque<String> inputQueue;
    private volatile boolean exitRequested;
    private boolean gameExited = false;
    private TextField commandPromptField;
    private TextArea outputArea;
    private Thread gameThread;
    private List<String> autoCompleteOverrides = null;

    static void main(String[] args) {
        launch(args);
    }

    public void setAutoCompleteOverrides(List<String> autoCompleteOverrides) {
        Platform.runLater(() -> this.autoCompleteOverrides = autoCompleteOverrides);
    }

    public void removeAutoCompleteOverrides() {
        Platform.runLater(() -> this.autoCompleteOverrides = null);
    }

    public List<String> autocomplete(String text) {
        return Optional.ofNullable(autoCompleteOverrides)
                // Use autoCompleteOverrides if available
                .map(strings -> strings.stream().filter(suggestion -> suggestion.startsWith(text)).toList())
                // Fall back to command autocompletion
                .orElseGet(() -> Optional.ofNullable(this.instance).map(instance -> instance.state.autocomplete(text)).orElse(List.of()));
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.inputQueue = new BlockingArrayListDeque<>(32);
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("GameView.fxml")));
        Scene scene = new Scene(root);

        this.commandPromptField = (TextField) scene.lookup("#textPrompt");
        var executeButton = (Button) scene.lookup("#executeButton");
        this.outputArea = (TextArea) scene.lookup("#textView");
        this.outputArea.setEditable(false);
        this.outputArea.setWrapText(true);

        for (Direction direction : Direction.values()) {
            var direction_string = direction.name().toLowerCase();
            var button = (Button) scene.lookup("#" + direction_string + "Button");
            button.setOnAction(_ -> inputQueue.try_push_front("go " + direction_string));
            button.setDisable(true);
            directionButtons.put(direction, button);
        }

        Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> suggestionProvider = request -> this.autocomplete(request.getUserText());
        TextFields.bindAutoCompletion(this.commandPromptField, suggestionProvider);

        stage.setScene(scene);
        stage.show();

        (executeButton).setOnAction(_ -> submitInputFromPrompt());
        this.commandPromptField.setOnAction(_ -> submitInputFromPrompt());

        this.gameThread = new Thread(() -> {
            while (true) {
                try {
                    var maybe_instance = ZorkInstance.loadOrCreateNew(this);
                    if (maybe_instance.isEmpty()) {
                        shutdownThreads();
                        return;
                    }
                    this.instance = maybe_instance.get();
                    this.instance.state.registerUpdateHook(game -> {
                        var room_paths = game.getCurrentRoom().paths;
                        Platform.runLater(() -> {
                            for (var direction : Direction.values()) {
                                directionButtons.get(direction).setDisable(!room_paths.containsKey(direction));
                            }
                        });
                    });
                    instance.state.notifyUpdateHooks();
                    while (!WasExitRequested()) {
                        presentTextPrompt("Please enter the action you want to perform\n> ");
                        var line = consumeTextInput();
                        if (line.isEmpty()) {
                            shutdownThreads();
                            return;
                        }
                        instance.advanceGame(this, line.get());
                    }
                    if (!this.exitRequested) {
                        this.gameExited = false;
                        presentTextPrompt("Would you like to play again? (y/n)\n>");
                        var answer = consumeTextInput();
                        if (answer.isEmpty() || !answer.get().toLowerCase().startsWith("y")) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    // ensure UI shows unexpected errors
                    presentErrorMessage("Internal UI thread error: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                }
            }
            shutdownThreads();
        }, "Zork-GameThread");
        this.gameThread.start();
        stage.setOnCloseRequest(_ -> shutdownThreads());
    }

    private void submitInputFromPrompt() {
        if (this.commandPromptField == null) return;
        var text = this.commandPromptField.getText();
        if (text == null) text = "";
        this.commandPromptField.clear();
        this.inputQueue.try_push_front(text);
    }

    private void disableAndSaveMoveButtons() {
        Platform.runLater(() -> {
            for (var button : directionButtons.entrySet()) {
                savedButtonState.put(button.getKey(), button.getValue().isDisabled());
                button.getValue().setDisable(true);
            }
        });
    }

    private void restoreMoveButtons() {
        Platform.runLater(() -> {
            for (var button : directionButtons.entrySet()) {
                button.getValue().setDisable(savedButtonState.get(button.getKey()));
            }
        });
    }

    private void appendOutput(String text) {
        Platform.runLater(() -> {
            outputArea.appendText(text);
            // Scroll to the bottom
            outputArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    @Override
    public boolean WasExitRequested() {
        return gameExited || exitRequested;
    }

    @Override
    public void notifyOfCompletion() {
        gameExited = true;
    }

    public void shutdownThreads() {
        exitRequested = true;
        // Unblock the game thread if it's waiting for input
        inputQueue.try_push_front("");
        try {
            // Wake game thread if it's waiting on something else
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
        sb.append("Choose an option (1-").append(options.size()).append("):\n");
        appendOutput(sb.toString());

        disableAndSaveMoveButtons();
        setAutoCompleteOverrides(IntStream.range(1, options.size() + 1).mapToObj(String::valueOf).toList());
        var line = consumeTextInput();
        restoreMoveButtons();
        removeAutoCompleteOverrides();


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

        disableAndSaveMoveButtons();
        setAutoCompleteOverrides(IntStream.range(1, Optional.ofNullable(options).map(List::size).orElse(0) + 1).mapToObj(String::valueOf).toList());
        var line = consumeTextInput();
        restoreMoveButtons();
        removeAutoCompleteOverrides();

        if (line.isEmpty()) {
            return "";
        }
        var trimmed = line.get().trim();
        try {
            int idx = Integer.parseInt(trimmed);
            if (options != null && idx >= 1 && idx <= options.size()) {
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
            line = inputQueue.pop_back();
            appendOutput(line + "\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        if (WasExitRequested()) {
            return Optional.empty();
        }
        return Optional.of(line);
    }

    @Override
    public void presentTextPrompt(String prompt) {
        Platform.runLater(() -> {
            try {
                commandPromptField.setPromptText(prompt);
                commandPromptField.requestFocus();
            } catch (Exception ignored) {
            }
        });
        appendOutput(prompt);
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

    @Override
    public String toString() {
        return "UIController{" + "directionButtons=" + directionButtons + ", savedButtonState=" + savedButtonState + ", instance=" + instance + ", inputQueue=" + inputQueue + ", exitRequested=" + exitRequested + ", commandPromptField=" + commandPromptField + ", outputArea=" + outputArea + ", gameThread=" + gameThread + ", autoCompleteOverrides=" + autoCompleteOverrides + '}';
    }
}