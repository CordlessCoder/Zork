package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {
    @JsonProperty("description")
    private String description;
    @JsonProperty("name")
    private String name;
    @JsonIgnore
    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void useInInventory(GameState context) {
        context.controller.presentMessage("You try to use the " + name + ", but nothing happens.");
    }

    public void useInRoom(GameState context) {
        context.controller.presentMessage("You try to use the " + name + " in " + context.getCurrentRoom().name + ", but nothing happens.");
    }

    public void pickUp(GameState context) {
        if (!context.getCurrentRoom().takeItem(this.id)) {
            context.controller.presentMessage("I can't find this item!");
            return;
        }
        context.player.addItem(this.id);
        context.notifyUpdateHooks();
    }

    public void drop(GameState context) {
        if (!context.player.hasItem(this.id)) {
            context.controller.presentMessage("I don't have this!");
            return;
        }
        context.player.removeItem(this.id);
        context.getCurrentRoom().addItem(this.id);
        context.notifyUpdateHooks();

    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Item{" + "description='" + description + '\'' + ", name='" + name + '\'' + '}';
    }
}
