package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.file.Path;

public class Item {
    private String description;
    private String name;
    @JsonIgnore
    private String filename;


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
}
