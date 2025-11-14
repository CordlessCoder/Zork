package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.dirs.ProjectDirectories;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public class Zork {
    @JsonProperty("rooms")
    private HashMap<String, Room> rooms;
    @JsonProperty("items")
    private HashMap<String, Item> items;
    @JsonIgnore
    private boolean isExitRequested = false;
    @JsonProperty("player")
    private Player player;

    private Zork() {
    }

    public static Path getSaveDirectory() {
        var directories = ProjectDirectories.from("org", "example", "ZorkUL");
        var user_data_directory = directories.dataDir;
        return Path.of(user_data_directory, "saves");
    }

    public static Optional<File[]> listSaveFiles() {
        var saves_dir = getSaveDirectory().toFile();
        var save_files = saves_dir.listFiles();
        return Optional.ofNullable(save_files);
    }

    private static Zork loadState(String name) throws JacksonException {
        var saves_path = getSaveDirectory();
        var save_file_path = Path.of(saves_path.toString(), name + ".json");
        return new ObjectMapper().readValue(save_file_path, Zork.class);
    }

    public static Optional<Zork> loadInitialState() {
        Optional<Zork> game = Optional.empty();
        try {
            var state_stream = Objects.requireNonNull(Zork.class.getClassLoader().getResource("initial_state.json")).openStream();

            game = Optional.of(new ObjectMapper().readValue(state_stream, Zork.class));
            state_stream.close();
        } catch (NullPointerException p) {
            System.err.println("Unrecoverable error: Internal JSON file `initial_state.json` missing.");
        } catch (IOException e) {
            System.err.println("Unrecoverable error: Internal JSON file `initial_state.json` unavailable.");
            e.printStackTrace();
        }
        return game;
    }


    private void saveState(String name) throws JacksonException {
        var saves_path = getSaveDirectory();
        saves_path.toFile().mkdirs();
        var save_file_path = Path.of(saves_path.toString(), name + ".json");
        new ObjectMapper().writeValue(save_file_path, this);
    }

    public Room getCurrentRoom() {
        var room_name = player.getCurrentRoomName();
        return rooms.get(room_name);
    }

    public void fixupRoomNames() {
        for (var room : rooms.entrySet()) {
            String name = room.getKey();
            room.getValue().setName(name);
        }
    }

    public void play() {
        printWelcome();

        try (var input = new Scanner(System.in)) {
            while (!isExitRequested) {
                System.out.print("> ");
                var line = input.nextLine();
                var cmd = CommandRegistry.parse(line);
                if (cmd.isEmpty()) {
                    System.out.println("Unknown command.");
                    continue;
                }
                cmd.get().execute(this);
            }
        }
        System.out.println("Thank you for playing. Goodbye.");
    }

    private void printWelcome() {
        System.out.println();
        System.out.println("Welcome to the University adventure!");
        System.out.println("Type 'help' if you need help.");
        System.out.println();
        lookMessage();
    }

    void printHelp() {
        System.out.println("You are lost. You are alone. You wander around the university.");
        System.out.println("Possible commands are:");
        System.out.println(CommandRegistry.describeCommands());
    }

    void takeItem(String item_name) {
        if (!getCurrentRoom().takeItem(item_name)) {
            System.out.println("I can't find this item!");
            return;
        }
        player.addItem(item_name);
    }

    void dropItem(String item_name) {
        if (!player.hasItem(item_name)) {
            System.out.println("I don't have this!");
            return;
        }
        player.removeItem(item_name);
        getCurrentRoom().addItem(item_name);
    }


    void lookMessage() {
        System.out.println("Your items: " + player.getItemString());
        System.out.println(rooms.get(player.getCurrentRoomName()).getLongDescription());
    }

    void goTo(String place) {
        var parsed_direction = Direction.fromString(place);
        if (parsed_direction.isEmpty()) {
            System.out.println("There is no door!");
            return;
        }
        Direction direction = parsed_direction.get();

        Room nextRoom = rooms.get(this.getCurrentRoom().getExitName(direction));

        if (nextRoom == null) {
            System.out.println("There is no door!");
            return;
        }
        player.setCurrentRoomName(nextRoom.getName());
        lookMessage();
    }

    void setExitRequested() {
        this.isExitRequested = true;
    }


    @Override
    public String toString() {
        return "ZorkUL{" + "rooms=" + rooms + ", items=" + items + ", isExitRequested=" + isExitRequested + ", player=" + player + '}';
    }
}
