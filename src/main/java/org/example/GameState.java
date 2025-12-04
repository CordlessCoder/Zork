package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.function.Consumer;

// All state that requires overridden methods to work(items, rooms) is stored in a Typed* class, which is
// loaded into a type-erased map at runtime
public class GameState {
    @JsonIgnore
    private final ArrayList<Consumer<GameState>> updateHooks = new ArrayList<>();
    @JsonIgnore
    public boolean isExitRequested = false;
    @JsonIgnore
    public ViewController controller;
    @JsonIgnore
    public String save_name;
    @JsonIgnore
    public MapLayout layout;
    @JsonProperty("typed_rooms")
    TypedRooms typed_rooms;
    @JsonProperty("generic_rooms")
    HashMap<String, Room> generic_rooms;
    @JsonIgnore
    HashMap<String, Room> loaded_rooms = new HashMap<>();
    @JsonProperty("typed_items")
    TypedItems typed_items;
    @JsonProperty("inert_items")
    HashMap<String, Item> inert_items;
    @JsonIgnore
    HashMap<String, Item> loaded_items = new HashMap<>();
    @JsonProperty("player")
    Player player;

    private GameState() {
    }


    /// Registers a function that shall be called whenever the game state is modified
    public void registerUpdateHook(Consumer<GameState> hook) {
        updateHooks.add(hook);
    }

    public void notifyUpdateHooks() {
        for (var hook : updateHooks) {
            hook.accept(this);
        }
    }

    public Room getCurrentRoom() {
        var room_name = player.getCurrentRoomId();
        return loaded_rooms.get(room_name);
    }

    ///  This method should be called after updating the room structure.
    public void roomUpdateHook() {
        loaded_rooms.putAll(generic_rooms);
        loaded_rooms.putAll(typed_rooms.toRoomMap());
        for (var room : loaded_rooms.entrySet()) {
            String name = room.getKey();
            room.getValue().setId(name);
        }
        layout = new MapLayout(loaded_rooms);
        notifyUpdateHooks();
    }

    public Optional<Item> lookupItem(String name) {
        return Optional.ofNullable(loaded_items.get(name)).or(() -> {
            var name_lowercase = name.toLowerCase();
            for (var item : loaded_items.values()) {
                if (item.getName().toLowerCase().equals(name_lowercase)) {
                    return Optional.of(item);
                }
            }
            return Optional.empty();
        });
    }

    ///  This method should be called after updating the item structure.
    public void itemUpdateHook() {
        loaded_items.putAll(typed_items.toItemMap());
        loaded_items.putAll(inert_items);
        for (var item : loaded_items.entrySet()) {
            String name = item.getKey();
            item.getValue().setId(name);
        }
        notifyUpdateHooks();
    }

    public List<String> autocomplete(String text) {
        return CommandRegistry.autocomplete(this, text);
    }

    void showHelp() {
        controller.presentMessage("You are lost. You are alone. You wander around the university.");
        controller.presentMessage("Possible commands are:");
        controller.presentMessage(CommandRegistry.describeCommands());
    }

    void useItem(String item_name) {
        var maybe_item = lookupItem(item_name);
        if (maybe_item.isEmpty()) {
            controller.presentUrgentMessage("What's a \"" + item_name + "\"?");
            return;
        }
        var item = maybe_item.get();
        if (!player.hasItem(item.getId())) {
            if (getCurrentRoom().items.contains(item_name)) {
                item.useInRoom(this);
                return;
            };
            controller.presentUrgentMessage("You don't have this!");
            return;
        }
        item.useInInventory(this);
    }



    void mapMessage() {
        StringBuilder out = new StringBuilder();
        out.append("Map:\n");
        int room_name_length = layout.layout.streamVal().filter(Objects::nonNull).mapToInt(room -> room.name.length()).max().orElse(0);
        char[] delimiters = {'[', ']'};
        var no_room = " ".repeat(room_name_length + delimiters.length);
        var horizontal_connector = "<=>";
        var no_horizontal_connector = " ".repeat(horizontal_connector.length());
        var vertical_connector = StringUtils.centerString("|", room_name_length + delimiters.length);
        var current_room = this.player.getCurrentRoomId();

        for (int row_idx = 0; row_idx < layout.layout.getHeight(); row_idx++) {
            var row = layout.layout.row(row_idx);
            // Print upper/north connectors
            for (int col_idx = 0; row_idx > 0 && col_idx < layout.layout.getWidth(); col_idx++) {
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
            out.append('\n');
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
                    var connector = room.getExitName(Direction.West) != null ? horizontal_connector : no_horizontal_connector;
                    out.append(connector);
                }
                var display_delimiters = room.getId().equals(current_room) ? delimiters : new char[]{' ', ' '};
                var display_name = StringUtils.centerString(display_delimiters[0] + room.name + display_delimiters[1], room_name_length);
                out.append(display_name);
            }
            out.append('\n');
        }
        controller.presentMessage(out.toString());
    }

    void lookMessage() {
        controller.presentMessage("Your items: " + player.getItemString(this));
        controller.presentMessage(loaded_rooms.get(player.getCurrentRoomId()).getLongDescription(this));
    }

    void goTo(String place) {
        var parsed_direction = Direction.fromString(place);
        if (parsed_direction.isEmpty()) {
            controller.presentMessage("There is no door!");
            return;
        }
        Direction direction = parsed_direction.get();

        Room nextRoom = loaded_rooms.get(this.getCurrentRoom().getExitName(direction));
        String old_room = this.getCurrentRoom().getId();

        if (nextRoom == null) {
            controller.presentMessage("There is no door!");
            return;
        }
        nextRoom.onEnter(this);
        notifyUpdateHooks();
        if (this.controller.WasExitRequested()) {
            return;
        }
        if (!old_room.equals(player.getCurrentRoomId())) {
            lookMessage();
        }
    }

    void setExitRequested() {
        this.isExitRequested = true;
        this.controller.notifyOfCompletion();
    }


    @Override
    public String toString() {
        return "Zork{" + "rooms=" + loaded_rooms + ", items=" + loaded_items + ", isExitRequested=" + isExitRequested + ", player=" + player + ", controller=" + controller + ", save_name='" + save_name + '\'' + '}';
    }
}
