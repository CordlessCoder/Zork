package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class UIController extends Application implements ViewController {
    ZorkInstance instance = null;

    // New state for ViewController implementation
    private volatile boolean exitRequested = false;
    private TextField commandPromptField = null;
    private Pane mapPaneField = null;
    private TextArea outputArea = null;
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private Thread gameThread = null;
    private final String INPUT_EXIT_SENTINEL = "__UI_EXIT__";

    static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("GameView.fxml")));
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Scene scene = new Scene(root, 640, 480);

        // locate UI controls by id (set in FXML)
        var mapPane = (Pane) scene.lookup("#textViewPane");
        this.commandPromptField = (TextField) scene.lookup("#textPrompt");
        var executeButton = (javafx.scene.control.Button) scene.lookup("#executeButton");

        this.outputArea = new TextArea();
        this.outputArea.setEditable(false);
        this.outputArea.setWrapText(true);
//        this.outputArea.setPrefWidth(mapPane.getPrefWidth() <= 0 ? 436 : mapPane.getPrefWidth());
//        this.outputArea.setPrefHeight(mapPane.getPrefHeight() <= 0 ? 238 : mapPane.getPrefHeight());
        mapPane.getChildren().add(outputArea);

        this.mapPaneField = mapPane;

        Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> suggestionProvider = request -> {
            if (this.instance == null) {
                return List.of();
            }
            return this.instance.state.autocomplete(request.getUserText());
        };
        TextFields.bindAutoCompletion(this.commandPromptField, suggestionProvider);

        stage.setScene(scene);
        stage.show();

        ( executeButton).setOnAction(evt -> submitInputFromPrompt());
        this.commandPromptField.setOnAction(evt -> submitInputFromPrompt());

        // start background thread to run the interactive game loop (so UI thread remains responsive)
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
        this.gameThread.setDaemon(true);
        this.gameThread.start();
    }

    private void submitInputFromPrompt() {
        if (this.commandPromptField == null) return;
        var text = this.commandPromptField.getText();
        // push to queue so background thread can consume
        if (text == null) text = "";
        this.commandPromptField.clear();
        // best-effort offer (non-blocking)
        this.inputQueue.offer(text);
    }

    @FXML
    private void leftButtonHandler(ActionEvent event) {
        System.out.println("left");
    }

    @FXML
    private void rightButtonHandler(ActionEvent event) {
        System.out.println("right");
    }

    @FXML
    private void upButtonHandler(ActionEvent event) {
        System.out.println("up");
    }

    @FXML
    private void downButtonHandler(ActionEvent event) {
        System.out.println("down");
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
        // offer sentinel to unblock any waiting input consumers
        inputQueue.offer(INPUT_EXIT_SENTINEL);
        // try to interrupt game thread so it wakes if blocked elsewhere
        if (gameThread != null) {
            try {
                gameThread.interrupt();
            } catch (Exception ignored) {
            }
        }
        // exit JavaFX application
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
        // display options in the output area
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < options.size(); i++) {
            sb.append((i + 1)).append(") ").append(options.get(i)).append("\n");
        }
        appendOutput(sb.toString());
        appendOutput("Choose an option (1-" + options.size() + "):\n");

        // block until the user submits input
        String line;
        try {
            line = inputQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        if (INPUT_EXIT_SENTINEL.equals(line) || WasExitRequested()) {
            return Optional.empty();
        }
        var trimmed = line.trim();
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
            appendOutput(line + "\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        if (INPUT_EXIT_SENTINEL.equals(line) || WasExitRequested()) {
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