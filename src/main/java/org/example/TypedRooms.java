package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TypedRooms {
    @JsonProperty("winning_room")
    WinningRoom winning_room;

    private TypedRooms() {
    }

    public Map<String, Room> toRoomMap() {
        return Map.of("winning_room", winning_room);
    }
}


class WinningRoom extends Room {

    @Override
    public void onEnter(GameState context) {
        if (!context.player.hasItem("winning_key")) {
            context.controller.presentUrgentMessage("You pull on the handle, but nothing happens.");
            return;
        }
        super.onEnter(context);
        context.controller.presentMessage("You win!");
        context.setExitRequested();
    }
}