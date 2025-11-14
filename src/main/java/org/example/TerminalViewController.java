package org.example;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TerminalViewController implements ViewController {
    private final Scanner scanner = new Scanner(System.in);
    private volatile boolean exitRequested = false;

    @Override
    public boolean WasExitRequested() {
        return exitRequested;
    }

    @Override
    public void notifyOfCompletion() {
        exitRequested = true;
        try {
            scanner.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    public <T> Optional<T> presentSelectionList(List<T> options) {
        if (options == null || options.isEmpty()) {
            System.out.println("(no options)");
            return Optional.empty();
        }
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ") " + options.get(i));
        }
        System.out.print("Choose an option (1-" + options.size() + ") or type input: ");
        var lineOpt = consumeTextInput();
        if (lineOpt.isEmpty()) return Optional.empty();
        var line = lineOpt.get().trim();
        int idx;
        try {
            idx = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Input was not a number.");
            return Optional.empty();
        }
        if (idx < 1 || idx > options.size()) {
            System.out.println("Invalid selection number.");
            return Optional.empty();
        }
        return Optional.of(options.get(idx - 1));
    }

    @Override
    public String presentTextSelectionListWithPrompt(List<String> options, String prompt) {
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ") " + options.get(i));
        }
        System.out.print("Choose an option number or " + prompt + "\n> ");
        var line = consumeTextInput().orElse("");
        var trimmed = line.trim();
        try {
            int idx = Integer.parseInt(trimmed);
            if (idx >= 1 && idx <= options.size()) {
                return options.get(idx - 1);
            }
        } catch (NumberFormatException ignored) {
        }
        return line;
    }

    @Override
    public Optional<String> consumeTextInput() {
        try {
            if (!scanner.hasNextLine()) return Optional.empty();
            String line = scanner.nextLine();
            return Optional.ofNullable(line);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void presentTextPrompt(String prompt) {
        System.out.print(prompt + "\n> ");
    }

    @Override
    public void presentMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void presentUrgentMessage(String message) {
        System.out.println("! " + message);
    }

    @Override
    public void presentErrorMessage(String message) {
        System.err.println("ERROR: " + message);
    }
}
