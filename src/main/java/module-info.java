module Zork {
    exports org.example to javafx.graphics;
    opens org.example to tools.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires io.github.classgraph;
    requires javafx.controls;
    requires javafx.graphics;
    requires tools.jackson.databind;
}