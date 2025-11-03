package org.example;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.io.IOException;
import java.util.HashMap;

public class ZorkUL {
    private final Parser parser;
    private final HashMap<String, Room> rooms = new HashMap<>();
    private final HashMap<String, Item> items = new HashMap<>();
    private Player player;

    public ZorkUL() {
        createRooms();
        parser = new Parser();
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
                Item item = null;
                try {
                    item = Item.fromReader(resource.getPath(), resource.open());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                items.put(item.getFilename(), item);
            }));
        }
        // create the player character and start outside
        try {
            player = Player.fromReader(ZorkUL.class.getClassLoader().getResource("player_initial_state.json").openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void play() {
        printWelcome();

        boolean finished = false;
        while (!finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
        System.out.println("Thank you for playing. Goodbye.");
    }

    private void printWelcome() {
        System.out.println();
        System.out.println("Welcome to the University adventure!");
        System.out.println("Type 'help' if you need help.");
        System.out.println();
        System.out.println(this.getCurrentRoom().getLongDescription());
    }

    private boolean processCommand(Command command) {
        String commandWord = command.getCommandWord();

        if (commandWord == null) {
            System.out.println("I don't understand your command...");
            return false;
        }

        switch (commandWord) {
            case "help":
                printHelp();
                break;
            case "go":
                goRoom(command);
                break;
            case "quit":
                if (command.hasSecondWord()) {
                    System.out.println("Quit what?");
                    return false;
                } else {
                    return true; // signal to quit
                }
            default:
                System.out.println("I don't know what you mean...");
                break;
        }
        return false;
    }

    private void printHelp() {
        System.out.println("You are lost. You are alone. You wander around the university.");
        System.out.print("Your command words are: ");
        parser.showCommands();
    }

    private void goRoom(Command command) {
        if (!command.hasSecondWord()) {
            System.out.println("Go where?");
            return;
        }

        String direction = command.getSecondWord();

        Room nextRoom = rooms.get(this.getCurrentRoom().getExitName(direction));

        if (nextRoom == null) {
            System.out.println("There is no door!");
        } else {
            player.setCurrentRoomName(nextRoom.getFilename());
            System.out.println(rooms.get(player.getCurrentRoomName()).getLongDescription());
        }
    }

    @Override
    public String toString() {
        return "ZorkUL{" + "parser=" + parser + ", rooms=" + rooms + ", player=" + player + '}';
    }
}
