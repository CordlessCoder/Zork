package org.example;

import java.util.Arrays;

// Used as a wrapper when packaging into an uber-jar using Apache Shade
public class UIMain {
    static void main(String[] args) {
        if (Arrays.asList(args).contains("--cli")) {
            CLI.main(args);
        } else {
            UIController.main(args);
        }
    }
}
