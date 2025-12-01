package org.example;

import java.util.HashMap;

public class MapLayout {
    public final Matrix<Room> layout;

    public MapLayout(HashMap<String, Room> rooms) {
        var locations = getLocations(rooms);
        int[] dimensions = {0, 0};
        locations.forEach((name, location) -> {
            dimensions[0] = Math.max(dimensions[0], location.row + 1);
            dimensions[1] = Math.max(dimensions[1], location.column + 1);
        });
        this.layout = new Matrix<>(dimensions[1], dimensions[0] );
        locations.forEach((name, location) -> {
            this.layout.set(location.row, location.column, location.room);
        });
    }

    private HashMap<String, RoomWithLocation> getLocations(HashMap<String, Room> rooms) {
        if (rooms.isEmpty()) {
            return new HashMap<>();
        }
        HashMap<String, RoomWithLocation> locations = new HashMap<>();
        rooms.forEach((name, room) -> locations.put(name, new RoomWithLocation(room)));
        var entry_room = locations.entrySet().iterator().next().getKey();
        relativeLocationRoomVisitor(locations, 0, 0, entry_room);
        // pair of minimum value of rows and columns
        final int[] coordinate_bounds = {0, 0, 0, 0};
        locations.forEach((name, location) -> {
            coordinate_bounds[0] = Math.min(coordinate_bounds[0], location.row);
            coordinate_bounds[1] = Math.min(coordinate_bounds[1], location.column);
            assert location.visited;
        });
        int row_offset = -coordinate_bounds[0];
        int col_offset = -coordinate_bounds[1];
        locations.forEach((name, location) -> {
            location.row += row_offset;
            location.column += col_offset;
        });
        return locations;
    }

    // DFS over the rooms to find their relative locations
    private void relativeLocationRoomVisitor(HashMap<String, RoomWithLocation> locations, int row, int column, String name) {
        var location = locations.get(name);
        if (location.visited) {
            return;
        }
        location.visited = true;
        location.row = row;
        location.column = column;
        var room = location.room;
        room.paths.forEach((direction, target) -> {
            switch (direction) {
                case North -> relativeLocationRoomVisitor(locations, row - 1, column, target);
                case South -> relativeLocationRoomVisitor(locations, row + 1, column, target);
                case East -> relativeLocationRoomVisitor(locations, row, column + 1, target);
                case West -> relativeLocationRoomVisitor(locations, row, column - 1, target);
            }
        });
    }

    @Override
    public String toString() {
        return "MapLayout{" +
                "layout=" + layout +
                '}';
    }
}

class RoomWithLocation {
    public final Room room;
    public int row;
    public int column;
    public boolean visited = false;

    public RoomWithLocation(Room room) {
        this.room = room;
    }
}