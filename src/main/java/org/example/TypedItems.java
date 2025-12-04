package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TypedItems {
    @JsonProperty("winning_key")
    WinningKey winning_key;

    private TypedItems() {
    }

    public Map<String, Item> toItemMap() {
        return Map.of("winning_key", winning_key);
    }
}

class WinningKey extends Item {
    @Override
    public void use(GameState context) {
        context.controller.presentMessage("You were born a winner!");
    }
}
