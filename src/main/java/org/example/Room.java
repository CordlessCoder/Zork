package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.EnumMap;
import java.util.HashSet;

public class Room {
    @JsonProperty("items")
    private HashSet<String> items = new HashSet<>();
    @JsonProperty("description")
    private String description;
    @JsonProperty("paths")
    private EnumMap<Direction, String> paths = new EnumMap<>(Direction.class);
    @JsonIgnore
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    public void setExit(Direction direction, String neighbor) {
        paths.put(direction, neighbor);
    }

    public String getExitName(Direction direction) {
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
        for (final var direction : paths.keySet()) {
            sb.append(direction).append(" ");
        }
        return sb.toString().trim();
    }

    public String getLongDescription() {
        return "You are " + description + ".\nExits: " + getExitString() + "\nItems: " + getItemString();
    }

    @Override
    public String toString() {
        return "Room{" +
                "items=" + items +
                ", description='" + description + '\'' +
                ", paths=" + paths +
                '}';
    }
}
