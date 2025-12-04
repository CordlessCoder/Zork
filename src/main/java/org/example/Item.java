package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {
    @JsonProperty("description")
    private String description;
    @JsonProperty("name")
    private String name;

    public void use(GameState context) {
        context.controller.presentMessage("You try to use the " + name + ", but nothing happens.");
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Item{" +
                "description='" + description + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
