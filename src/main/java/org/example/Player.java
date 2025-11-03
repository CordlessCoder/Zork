package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;

public class Player {
    private String name;
    @JsonProperty("room")
    private String currentRoom;

    public static Player fromReader(InputStream reader) {
        return new ObjectMapper().readValue(reader, Player.class);
    }

    public String getName() {
        return name;
    }

    public String getCurrentRoomName() {
        return currentRoom;
    }

    public void setCurrentRoom(String room) {
        this.currentRoom = room;
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
        return "Player{" +
                "name='" + name + '\'' +
                ", currentRoom='" + currentRoom + '\'' +
                '}';
    }
}
