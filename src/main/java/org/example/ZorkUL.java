package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class ZorkUL {
    @JsonProperty("rooms")
    private HashMap<String, Room> rooms;
    @JsonProperty("items")
    private HashMap<String, Item> items;
    @JsonIgnore
    private boolean isExitRequested = false;
    @JsonProperty("player")
    private Player player;

    private ZorkUL() {
    }

    static void main(String[] args) {
        ZorkUL game;
        try {
            var state_stream = Objects.requireNonNull(ZorkUL.class.getClassLoader().getResource("initial_state.json")).openStream();
            game = ZorkUL.readFrom(state_stream);
            state_stream.close();
        } catch (NullPointerException p) {
            System.err.println("Unrecoverable error: Internal JSON file `initial_state.json` missing.");
            return;
        } catch (IOException e) {
            System.err.println("Unrecoverable error: Internal JSON file `initial_state.json` unavailable.");
            e.printStackTrace();
            return;
        }
        game.setupRoomNames();
        System.out.println(game);
        game.play();
    }

    public Room getCurrentRoom() {
        var room_name = player.getCurrentRoomName();
        return rooms.get(room_name);
    }

    private void setupRoomNames() {
        for (var room : rooms.entrySet()) {
            String name = room.getKey();
            room.getValue().setName(name);
        }
    }

    private static ZorkUL readFrom(InputStream input) throws JacksonException {
        return new ObjectMapper().readValue(input, ZorkUL.class);
//        try (ScanResult scanResult = new ClassGraph().acceptPathsNonRecursive("rooms/").scan()) {
//            scanResult.getResourcesWithExtension("json").forEach((resource -> {
//                try {
//                    Room room = Room.fromReader(resource.getPath(), resource.open());
//                    System.out.println(room);
//                    rooms.put(room.getFilename(), room);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }));
//        }
//        try (ScanResult scanResult = new ClassGraph().acceptPathsNonRecursive("items/").scan()) {
//            scanResult.getResourcesWithExtension("json").forEach((resource -> {
//                try {
//                    Item item = Item.fromReader(resource.getPath(), resource.open());
//                    items.put(item.getFilename(), item);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }));
//        }
//        try {
//            player = Player.fromReader(ZorkUL.class.getClassLoader().getResource("player_initial_state.json").openStream());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
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
