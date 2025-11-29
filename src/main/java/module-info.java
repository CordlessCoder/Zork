module Zork {
    opens org.example to javafx.graphics,javafx.fxml,tools.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires org.controlsfx.controls;
    requires javafx.controls;
    requires javafx.graphics;
    requires tools.jackson.databind;
    requires dev.dirs;
    requires javafx.fxml;
    requires tools.jackson.core;
}
