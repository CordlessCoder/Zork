package org.example;

import java.util.List;
import java.util.Optional;

public interface ViewController {
    /// Checks if the user requested the app be exited.
    boolean WasExitRequested();

    ///  Notifies the View/Controller that the game has stopped running.
    void notifyOfCompletion();

    ///  Presents the user with a list of options.
    <T> Optional<T> presentSelectionList(List<T> options);

    ///  Presents the user with a list of options or a text-input-based prompt.
    String presentTextSelectionListWithPrompt(List<String> options, String prompt);

    ///  Returns a single line provided by the user, if one was provided.
    Optional<String> consumeTextInput();

    ///  Presents the user with a text message with the intent of getting a response from the user.
    void presentTextPrompt(String prompt);

    ///  Presents the user with a simple text message, as part of the game's narrative.
    void presentMessage(String message);

    ///  Presents the user with a simple highlighted message, for example, on invalid user input.
    void presentUrgentMessage(String message);

    ///  Presents the user with an error message, to be used for internal errors.
    void presentErrorMessage(String message);
}
