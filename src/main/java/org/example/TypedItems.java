package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

enum PizzaStatus {
    Cooking, Burnt, Ready, Taken
}

enum PizzaItemState {
    Good, Burnt
}

public class TypedItems {
    @JsonProperty("keys")
    Keys keys;

    @JsonProperty("computer")
    Computer computer;

    @JsonProperty("oven")
    Oven oven;

    @JsonProperty("pizza")
    Pizza pizza;

    @JsonProperty("mold")
    BlackMold mold;

    private TypedItems() {
    }

    public Map<String, Item> toItemMap() {
        return Map.of("keys", keys, "computer", computer, "oven", oven, "pizza", pizza, "mold", mold);
    }
}

class Keys extends Item {
    boolean wasTaken = false;

    @Override
    public void pickUp(GameState context) {
        if (!wasTaken) {
            var oven = context.typed_items.oven;
            oven.status = oven.status == PizzaStatus.Cooking ? PizzaStatus.Ready : oven.status;
        }
        wasTaken = true;
        super.pickUp(context);
    }

    @Override
    public void useInInventory(GameState context) {
        var room = context.getCurrentRoom();
        if (!room.getId().equals("hallway")) {
            context.controller.presentUrgentMessage("You can't use the keys here.");
            return;
        }
        var outdoors = context.typed_rooms.outdoors;
        if (!outdoors.locked) {
            context.controller.presentUrgentMessage("The door is already unlocked.");
        }
        outdoors.locked = false;
        context.controller.presentMessage("You use the keys to unlock the door.");
    }
}

class Computer extends Item {
    @Override
    public void pickUp(GameState context) {
        context.controller.presentMessage("You are too weak to pick up the computer.");
    }

    @Override
    public void useInRoom(GameState context) {
        context.controller.presentMessage("You sit behind the desk, having decided that going outside isn't worth it.");
        context.controller.presentMessage("You play some video games until you fall asleep, having wasted the day away.");
        context.controller.presentUrgentMessage("You got the bad ending. Press enter to exit.");
        context.controller.consumeTextInput();
        context.setExitRequested();
    }
}

class Oven extends Item {
    PizzaStatus status = PizzaStatus.Cooking;

    @Override
    public void pickUp(GameState context) {
        context.controller.presentMessage("You are too weak to pick up the oven.");
    }

    @Override
    public void useInRoom(GameState context) {
        switch (status) {
            case Cooking -> {
                context.controller.presentMessage("The pizza is still cooking.");
            }
            case Ready -> {
                context.controller.presentMessage("You open the oven and take out the perfectly cooked pizza.");
                context.typed_items.pizza = new Pizza(PizzaItemState.Good);
                context.player.addItem("pizza");
                status = PizzaStatus.Taken;
            }
            case Burnt -> {
                context.controller.presentMessage("You open the oven in a hurry, and are hit by a wave of smoke.");
                context.controller.presentMessage("You burnt the pizza.");
                context.typed_items.pizza = new Pizza(PizzaItemState.Burnt);
                context.player.addItem("pizza");
                status = PizzaStatus.Taken;
            }
        }
    }
}

class Pizza extends Item {
    PizzaItemState state = PizzaItemState.Good;
    boolean triedToEatBurnt = false;

    public Pizza() {
    }

    public Pizza(PizzaItemState state) {
        this.state = state;
    }

    @Override
    public void useInRoom(GameState context) {
        context.controller.presentMessage("You think about eating the " + this.getName().toLowerCase() + " off the floor, but realize you're above that.");
    }

    @Override
    public void useInInventory(GameState context) {
        switch (state) {
            case Good -> {
                context.controller.presentMessage("You enjoy a well cooked pizza, preparing you for the dangerous journey outside.");
                context.player.removeItem("pizza");
            }
            case Burnt -> {
                if (triedToEatBurnt) {
                    context.controller.presentMessage("You decide to eat the burnt pizza, powering through the smell. You lose all will to live.");
                    context.player.removeItem("pizza");
                }
                context.controller.presentMessage("The smell of the burnt pizza is overwhelming, and you do not manage to eat it.");
                triedToEatBurnt = true;
            }
        }
    }

    @Override
    public String getName() {
        return switch (state) {
            case Good -> "Pizza";
            case Burnt -> "Burnt Pizza";
        };
    }
}

class BlackMold extends Item {
    boolean triedToEat = false;

    @Override
    public void pickUp(GameState context) {
        context.controller.presentMessage("You pick up the black mold... Why would you do this?");
        super.pickUp(context);
    }

    @Override
    public void useInRoom(GameState context) {
        context.controller.presentMessage("You realize that \"using\" the black mold is a bad idea.");
    }

    @Override
    public void useInInventory(GameState context) {
        if (!triedToEat) {
            context.controller.presentMessage("You look at the black mold, and realize it looks kind of tasty.");
            context.controller.presentMessage("One bite couldn't hurt, right?");
            triedToEat = true;
            return;
        }
        context.controller.presentMessage("You bite into the black mold. It tastes great! You realize that all you need in this life is to find more black mold to eat.");
        context.controller.presentMessage("Throughout the rest of the day you binge eat a kilogram of black mold, and die.\nPress enter to exit.");
        context.controller.consumeTextInput();
        context.setExitRequested();
    }
}