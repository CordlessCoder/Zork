package org.example;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class ZorkUL {
    private final HashMap<String, Room> rooms = new HashMap<>();
    private final HashMap<String, Item> items = new HashMap<>();
    private boolean isExitRequested = false;
    private Player player;

    public ZorkUL() {
        createRooms();
    }

    public static void main(String[] args) {
        ZorkUL game = new ZorkUL();
        System.out.println(game);
        game.play();
    }

    public Room getCurrentRoom() {
        var room_name = player.getCurrentRoomName();
        return rooms.get(room_name);
    }

    private void createRooms() {
        try (ScanResult scanResult = new ClassGraph().acceptPathsNonRecursive("rooms/").scan()) {
            scanResult.getResourcesWithExtension("json").forEach((resource -> {
                try {
                    Room room = Room.fromReader(resource.getPath(), resource.open());
                    System.out.println(room);
                    rooms.put(room.getFilename(), room);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        try (ScanResult scanResult = new ClassGraph().acceptPathsNonRecursive("items/").scan()) {
            scanResult.getResourcesWithExtension("json").forEach((resource -> {
                try {
                    Item item = Item.fromReader(resource.getPath(), resource.open());
                    items.put(item.getFilename(), item);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        try {
            player = Player.fromReader(ZorkUL.class.getClassLoader().getResource("player_initial_state.json").openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        player.setCurrentRoomName(nextRoom.getFilename());
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
