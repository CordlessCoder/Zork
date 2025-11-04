package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;

public class Room {
    @JsonProperty("items")
    private HashSet<String> items = new HashSet<>();
    private String description;
    @JsonProperty("paths")
    private HashMap<String, String> paths = new HashMap<>(); // Map direction to neighboring Room
    @JsonIgnore
    private String filename;

    public void writeAt(String room_directory_path) {
        var path = Path.of(room_directory_path, filename);
        new ObjectMapper().writeValue(path, this);
    }
    public static Room fromReader(String path, InputStream input) {
        var room = new ObjectMapper().readValue(input, Room.class);
        room.filename = Path.of(path).getFileName().toString().replaceFirst("[.][^.]+$", "");
        return room;
    }

    public void addItem(String itemName) {
        items.add(itemName);
    }

    public boolean takeItem(String itemName) {
        return items.remove(itemName);
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

    public String getItemString() {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item).append(" ");
        }
        return sb.toString().trim();
    }

    public String getExitString() {
        StringBuilder sb = new StringBuilder();
        for (String direction : paths.keySet()) {
            sb.append(direction).append(" ");
        }
        return sb.toString().trim();
    }

    public String getLongDescription() {
        return "You are " + description + ".\nExits: " + getExitString() + "\nItems: " + getItemString();
    }

    @Override
    public String toString() {
        return "Room{" + "description='" + description + '\'' + ", items=" + items + ", filename='" + filename + '\'' + ", paths=" + paths + '}';
    }

    public String getFilename() {
        return filename;
    }
}
