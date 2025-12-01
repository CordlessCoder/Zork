package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GameState {
    @JsonIgnore
    public boolean isExitRequested = false;
    @JsonIgnore
    public ViewController controller;
    @JsonIgnore
    public String save_name;
    @JsonIgnore
    public MapLayout layout;
    @JsonProperty("rooms")
    HashMap<String, Room> rooms;
    @JsonProperty("items")
    HashMap<String, Item> items;
    @JsonProperty("player")
    Player player;

    private GameState() {
    }


    public Room getCurrentRoom() {
        var room_name = player.getCurrentRoomName();
        return rooms.get(room_name);
    }

    public void roomUpdateHook() {
        for (var room : rooms.entrySet()) {
            String name = room.getKey();
            room.getValue().setId(name);
        }
        layout = new MapLayout(rooms);
    }

    public List<String> autocomplete(String text) {
        return CommandRegistry.autocomplete(this, text);
    }


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


    void mapMessage() {
        StringBuilder out = new StringBuilder();
        out.append("Map:\n");
        int room_name_length = layout.layout.streamVal().filter(Objects::nonNull).mapToInt(room -> room.name.length()).max().orElse(0);
        char[] delimiters = {'[', ']'};
        var indent = "    ";
        var no_room = " ".repeat(room_name_length + delimiters.length);
        var horizontal_connector = "<=>";
        var no_horizontal_connector = " ".repeat(horizontal_connector.length());
        var vertical_connector = StringUtils.centerString("|", room_name_length + delimiters.length);
        var current_room = this.player.getCurrentRoomName();

        for (int row_idx = 0; row_idx < layout.layout.getHeight(); row_idx++) {
            var row = layout.layout.row(row_idx);
            out.append(indent);
            // Print upper/north connectors
            if (row_idx > 0) {
                for (int col_idx = 0; col_idx < layout.layout.getWidth(); col_idx++) {
                    var room = row.get(col_idx);

                    if (col_idx > 0) {
                        out.append(no_horizontal_connector);
                    }

                    if (room != null && room.getExitName(Direction.North) != null) {
                        out.append(vertical_connector);
                    } else {
                        out.append(no_room);
                    }
                }
            }
            out.append('\n');
            out.append(indent);
            // Print room names and left/west connectors
            for (int col_idx = 0; col_idx < layout.layout.getWidth(); col_idx++) {
                var room = row.get(col_idx);
                if (room == null) {
                    if (col_idx > 0) {
                        out.append(no_horizontal_connector);
                    }
                    out.append(no_room);
                    continue;
                }
                if (col_idx > 0) {
                    if (room.getExitName(Direction.West) != null) {
                        out.append(horizontal_connector);
                    } else {
                        out.append(no_horizontal_connector);
                    }
                }
                var display_name = StringUtils.centerString(room.name, room_name_length);
                var display_delimiters = room.getId().equals(current_room) ? delimiters : new char[]{' ', ' '};
                out.append(display_delimiters[0]).append(display_name).append(display_delimiters[1]);
            }
            out.append('\n');
        }
        controller.presentMessage(out.toString());
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
        player.setCurrentRoomName(nextRoom.getId());
        lookMessage();
    }

    void setExitRequested() {
        this.isExitRequested = true;
        this.controller.notifyOfCompletion();
    }


    @Override
    public String toString() {
        return "Zork{" + "rooms=" + rooms + ", items=" + items + ", isExitRequested=" + isExitRequested + ", player=" + player + ", controller=" + controller + ", save_name='" + save_name + '\'' + '}';
    }
}
