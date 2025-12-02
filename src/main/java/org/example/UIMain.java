package org.example;

// Used as a wrapper when packaging into an uber-jar using Apache Shade
public class UIMain {
    static void main(String[] args) {
        UIController.main(args);
    }
}
