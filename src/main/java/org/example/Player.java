package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;

public class Player {
    private final ArrayList<String> items = new ArrayList<>();
    private String name;
    @JsonProperty("room")
    private String currentRoomName;

    public static Player fromReader(InputStream reader) {
        return new ObjectMapper().readValue(reader, Player.class);
    }

    public String getName() {
        return name;
    }

    public String getCurrentRoomName() {
        return currentRoomName;
    }

    public void setCurrentRoomName(String room) {
        this.currentRoomName = room;
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
