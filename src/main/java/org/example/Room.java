package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class Room {
    private String description;
    private ArrayList<String> items;
    @JsonProperty("paths")
    private HashMap<String, String> paths = new HashMap<>(); // Map direction to neighboring Room
    @JsonIgnore
    private String filename;

    public static Room fromReader(String path, InputStream input) {
        var room = new ObjectMapper().readValue(input, Room.class);
        room.filename = Path.of(path).getFileName().toString().replaceFirst("[.][^.]+$", "");;
        return room;
    }

    public String getDescription() {
        return description;
    }

    public void setExit(String direction, String neighbor) {
        paths.put(direction, neighbor);
    }

    public String getExitName(String direction) {
        return paths.get(direction);
    }

    public String getExitString() {
        StringBuilder sb = new StringBuilder();
        for (String direction : paths.keySet()) {
            sb.append(direction).append(" ");
        }
        return sb.toString().trim();
    }

    public String getLongDescription() {
        return "You are " + description + ".\nExits: " + getExitString();
    }

    @Override
    public String toString() {
        return "Room{" +
                "description='" + description + '\'' +
                ", items=" + items +
                ", filename='" + filename + '\'' +
                ", paths=" + paths +
                '}';
    }

    public String getFilename() {
        return filename;
    }
}
