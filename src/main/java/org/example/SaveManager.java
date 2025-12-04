package org.example;

import dev.dirs.ProjectDirectories;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SaveManager {
    public static Path getSaveDirectory() {
        var directories = ProjectDirectories.from("org", "example", "Zork");
        var user_data_directory = directories.dataDir;
        return Path.of(user_data_directory, "saves");
    }

    public static Optional<File[]> listSaveFiles() {
        var saves_dir = getSaveDirectory().toFile();
        var save_files = saves_dir.listFiles();
        return Optional.ofNullable(save_files);
    }

    public static List<String> listSaveNames() {
        var save_files = SaveManager.listSaveFiles().orElse(new File[]{});
        return Arrays.stream(save_files).map(file -> RegexCommandHelper.trimJsonExtension(file.getName())).toList();
    }

    public static Path pathForSaveName(String name) {
        var saves_path = getSaveDirectory();
        return Path.of(saves_path.toString(), name + ".json");
    }

    public static GameState loadState(String name) throws JacksonException {
        var save_file_path = pathForSaveName(name);
        var game = new ObjectMapper().readValue(save_file_path, GameState.class);
        game.save_name = name;
        game.roomUpdateHook();
        game.itemUpdateHook();
        return game;
    }

    public static Optional<GameState> loadInitialState(String save_name) {
        Optional<GameState> game = Optional.empty();
        try {
            var state_stream = Objects.requireNonNull(GameState.class.getClassLoader().getResource("initial_state.json")).openStream();

            game = Optional.of(new ObjectMapper().readValue(state_stream, GameState.class));
            state_stream.close();
        } catch (NullPointerException p) {
            System.err.println("Unrecoverable error: Internal JSON file `initial_state.json` missing.");
        } catch (IOException e) {
            System.err.println("Unrecoverable error: Internal JSON file `initial_state.json` unavailable.");
            e.printStackTrace();
        }
        game.map(zork -> {
            zork.roomUpdateHook();
            zork.itemUpdateHook();
            zork.save_name = save_name;
            return zork;
        });

        return game;
    }


    public static void saveState(String name, GameState game) throws JacksonException {
        var saves_path = getSaveDirectory();
        saves_path.toFile().mkdirs();
        var save_file_path = pathForSaveName(name);
        new ObjectMapper().writeValue(save_file_path, game);
    }
}
