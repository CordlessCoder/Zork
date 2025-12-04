package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;

public class Player {
    @JsonProperty("items")
    final HashSet<String> items = new HashSet<>();
    @JsonProperty("name")
    private String name;
    @JsonProperty("room")
    private String currentRoomId;

    public String getName() {
        return name;
    }

    public String getItemString(GameState context) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(context.loaded_items.get(item).getName()).append(" ");
        }
        return sb.toString().trim();
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String room) {
        this.currentRoomId = room;
    }

    public void removeItem(String item_name) {
        items.remove(item_name);
    }

    public void addItem(String item_name) {
        items.add(item_name);
    }

    public boolean hasItem(String item_name) {
        return items.contains(item_name);
    }

    @Override
    public String toString() {
        return "Player{" + "name='" + name + '\'' + ", currentRoom='" + currentRoomId + '\'' + '}';
    }
}
