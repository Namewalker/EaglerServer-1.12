package com.eagler.immersiveportals;

import org.bukkit.Location;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public class PortalShape {
    // Detects if a portal shape made of bedrock exists around a location
    public static PortalShape detect(Location loc) {
        int[][] sizes = { {2,1}, {2,2}, {2,3}, {3,3}, {4,5} };
        // Scan a 5x5x5 cube around the location for possible frames
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    Location checkLoc = loc.clone().add(dx, dy, dz);
                    for (int[] size : sizes) {
                        List<Location> frameBlocks = getBedrockFrame(checkLoc, size[0], size[1]);
                        if (frameBlocks != null) {
                            return new PortalShape(frameBlocks);
                        }
                    }
                }
            }
        }
        return null;
    }
    // Returns list of frame blocks if a bedrock frame of given width/height exists, else null
    private static List<Location> getBedrockFrame(Location loc, int width, int height) {
        // Try both X and Z orientations
        for (int axis = 0; axis < 2; axis++) {
            List<Location> frameBlocks = new ArrayList<>();
            boolean valid = true;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Only add border blocks
                    if (x == 0 || x == width-1 || y == 0 || y == height-1) {
                        Location check = loc.clone().add(axis == 0 ? x : 0, y, axis == 1 ? x : 0);
                        if (!isBedrock(check)) {
                            valid = false;
                            break;
                        }
                        frameBlocks.add(check);
                    }
                }
                if (!valid) break;
            }
            if (valid && frameBlocks.size() >= 8) {
                return frameBlocks;
            }
        }
        return null;
    }
    private static boolean isBedrock(Location loc) {
        return loc.getBlock().getType() == Material.BEDROCK;
    }
    private final List<Location> blocks;
    public PortalShape(List<Location> blocks) {
        this.blocks = blocks;
    }
    public List<Location> getBlocks() { return blocks; }
}
