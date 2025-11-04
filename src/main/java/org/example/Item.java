package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.file.Path;

public class Item {
    @JsonProperty("description")
    private String description;
    @JsonProperty("name")
    private String name;
    @JsonIgnore
    private String filename;


    public void writeAt(String room_directory_path) {
        var path = Path.of(room_directory_path, filename);
        new ObjectMapper().writeValue(path, this);
    }
    public static Item fromReader(String path, InputStream reader) {
        var item = new ObjectMapper().readValue(reader, Item.class);
        item.filename = Path.of(path).getFileName().toString().replaceFirst("[.][^.]+$", "");
        return item;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "Item{" +
                "description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}
