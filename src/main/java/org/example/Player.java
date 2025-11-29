package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

public class Player {
    @JsonProperty("items")
    final HashSet<String> items = new HashSet<>();
    @JsonProperty("name")
    private String name;
    @JsonProperty("room")
    private String currentRoomName;

    public String getName() {
        return name;
    }

    public String getItemString() {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item).append(" ");
        }
        return sb.toString().trim();
    }

    public String getCurrentRoomName() {
        return currentRoomName;
    }

    public void setCurrentRoomName(String room) {
        this.currentRoomName = room;
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

//    public void move(String direction) {
//        String nextRoom = currentRoom.getExit(direction);
//        if (nextRoom != null) {
//            currentRoom = nextRoom;
//            System.out.println("You moved to: " + currentRoom.getDescription());
//        } else {
//            System.out.println("You can't go that way!");
//        }
//    }

    @Override
    public String toString() {
        return "Player{" + "name='" + name + '\'' + ", currentRoom='" + currentRoomName + '\'' + '}';
    }
}
