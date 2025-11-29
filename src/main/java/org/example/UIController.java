package org.example;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UIController extends Application {
    ZorkInstance instance = null;

    static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("GameView.fxml")));
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Scene scene = new Scene(root, 640, 480);
        var mapPane = (Pane) scene.lookup("#textViewPane");

        var commandPrompt = (TextField) scene.lookup("#textPrompt");
        // TODO: Autocomplete using game instance - perform after load?
        Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> suggestionProvider = request -> {
            if (instance == null) {
                return List.of();
            }
            return instance.state.autocomplete(request.getUserText());
        };

        TextFields.bindAutoCompletion(commandPrompt, suggestionProvider);
        var versionLabel = new Label("Using Java " + javaVersion + " and JavaFX " + javafxVersion);
        mapPane.getChildren().add(versionLabel);
        stage.setScene(scene);
        stage.show();
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

}