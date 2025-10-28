package com.example.bookportal.dimension;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.HashMap;
import java.util.Map;

public class DimensionManager {
    private final Map<String, World> customDimensions = new HashMap<>();

    public World createDimension(String bookName) {
        String normalizedBookName = normalizeBookName(bookName);
        if (customDimensions.containsKey(normalizedBookName)) {
            return customDimensions.get(normalizedBookName);
        }

        WorldCreator creator = new WorldCreator(normalizedBookName);
        World world = creator.createWorld();
        customDimensions.put(normalizedBookName, world);
        return world;
    }

    public World getDimension(String bookName) {
        return customDimensions.get(normalizeBookName(bookName));
    }

    private String normalizeBookName(String bookName) {
        // Implement normalization logic here (e.g., remove special characters, convert to lowercase)
        return bookName.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }

    public void unloadDimension(String bookName) {
        World world = customDimensions.remove(normalizeBookName(bookName));
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }
    }
}