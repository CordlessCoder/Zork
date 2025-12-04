package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class TypedRooms {
    @JsonProperty("outdoors")
    Outdoors outdoors;

    @JsonProperty("bathroom")
    Bathroom bathroom;

    private TypedRooms() {
    }

    public Map<String, Room> toRoomMap() {
        return Map.of("outdoors", outdoors, "bathroom", bathroom);
    }
}

class Bathroom extends Room {
    @JsonProperty("entered_count")
    int enteredCount = 0;

    @Override
    public String getDescription() {
        if (enteredCount < 2) {
            return "in the bathroom. The black mold in the corner seems to shift.";
        }
        return super.getDescription();
    }

    public void onEnter(GameState context) {
        enteredCount = Math.min(enteredCount + 1, 2);
        super.onEnter(context);
    }
}

class Outdoors extends Room {
    @JsonProperty("realized_there_is_no_key")
    private boolean realizedThereIsNoKey = false;
    @JsonProperty("locked")
    boolean locked = true;

    @Override
    public void onEnter(GameState context) {
        if (!context.player.hasItem("keys")) {
            if (!realizedThereIsNoKey) {
                context.controller.presentUrgentMessage("You realize you forgot the keys!");
                realizedThereIsNoKey = true;
                return;
            }
            context.controller.presentUrgentMessage("You still don't have the keys!");
            return;
        }
        if (locked) {
            context.controller.presentUrgentMessage("You have the key, but the door is locked!");
            return;
        }
        if (context.typed_items.oven.status == PizzaStatus.Ready) {
            context.controller.presentUrgentMessage("You realize you forgot the pizza!");
            context.controller.presentUrgentMessage("You notice a faint burnt smell.");
            context.typed_items.oven.status = PizzaStatus.Burnt;
            return;
        }
        super.onEnter(context);
        if (context.player.hasItem("mold")) {
            context.controller.presentMessage("You finally went outside, with black mold in hand. Who am I to judge?");
        } else {
            context.controller.presentMessage("You finally went outside!");
        }
        context.controller.presentMessage("Press enter to exit.");
        context.controller.consumeTextInput();
        context.setExitRequested();
    }
}