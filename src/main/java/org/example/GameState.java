package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

public class GameState {
    @JsonProperty("rooms")
    private HashMap<String, Room> rooms;
    @JsonProperty("items")
    private HashMap<String, Item> items;
    @JsonIgnore
    public boolean isExitRequested = false;
    @JsonProperty("player")
    private Player player;
    @JsonIgnore
    public ViewController controller;
    @JsonIgnore
    public String save_name;

    private GameState() {
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

    public List<String> autocomplete(String text) {
        return CommandRegistry.autocomplete(this, text);
    }


//    public void play() {
//        showWelcome();
//
//        try (var input = new Scanner(System.in)) {
//            while (!isExitRequested) {
//                var line = input.nextLine();
//                var cmd = CommandRegistry.parse(line);
//                if (cmd.isEmpty()) {
//                    controller.presentUrgentMessage("Unknown command.");
//                    continue;
//                }
//                cmd.get().execute(this);
//            }
//        }
//        controller.presentMessage("Thank you for playing. Goodbye.");
//    }

    private void showWelcome() {
        controller.presentMessage("Welcome to the University adventure!");
        controller.presentMessage("Type 'help' if you need help.");
        lookMessage();
    }

    void showHelp() {
        controller.presentMessage("You are lost. You are alone. You wander around the university.");
        controller.presentMessage("Possible commands are:");
        controller.presentMessage(CommandRegistry.describeCommands());
    }

    void takeItem(String item_name) {
        if (!getCurrentRoom().takeItem(item_name)) {
            controller.presentMessage("I can't find this item!");
            return;
        }
        player.addItem(item_name);
    }

    void dropItem(String item_name) {
        if (!player.hasItem(item_name)) {
            controller.presentMessage("I don't have this!");
            return;
        }
        player.removeItem(item_name);
        getCurrentRoom().addItem(item_name);
    }


    void lookMessage() {
        controller.presentMessage("Your items: " + player.getItemString());
        controller.presentMessage(rooms.get(player.getCurrentRoomName()).getLongDescription());
    }

    void goTo(String place) {
        var parsed_direction = Direction.fromString(place);
        if (parsed_direction.isEmpty()) {
            controller.presentMessage("There is no door!");
            return;
        }
        Direction direction = parsed_direction.get();

        Room nextRoom = rooms.get(this.getCurrentRoom().getExitName(direction));

        if (nextRoom == null) {
            controller.presentMessage("There is no door!");
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
        return "Zork{" +
                "rooms=" + rooms +
                ", items=" + items +
                ", isExitRequested=" + isExitRequested +
                ", player=" + player +
                ", controller=" + controller +
                ", save_name='" + save_name + '\'' +
                '}';
    }
}
