package org.example;

import java.io.File;

public class CLI {
    static void main(String[] args) {
        var save_files = Zork.listSaveFiles().orElse(new File[]{});
        Zork game = null;
        if (save_files.length == 0) {
            // Recovering from an error where the initial_state.json, an internal file is missing, is impossible
            game = Zork.loadInitialState().get();
//            System.out.println("Could not access saves directory");
//            return;
        }
        game.fixupRoomNames();
        System.out.println(game);
//        game.play();
    }
}
