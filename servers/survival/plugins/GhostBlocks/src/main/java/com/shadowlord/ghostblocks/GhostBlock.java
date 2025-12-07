package com.shadowlord.ghostblocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Objects;

public class GhostBlock {
    private final String worldName;
    private final int x, y, z;
    private final Material material;
    private final byte data;

    public GhostBlock(Location loc, Material material, byte data) {
        World w = loc.getWorld();
        this.worldName = w == null ? "world" : w.getName();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.material = material;
        this.data = data;
    }

    public String getWorldName() { return worldName; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public Material getMaterial() { return material; }
    public byte getData() { return data; }

    public Location toLocation() {
        World w = org.bukkit.Bukkit.getWorld(worldName);
        return new Location(w, x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GhostBlock)) return false;
        GhostBlock gb = (GhostBlock) o;
        return x == gb.x && y == gb.y && z == gb.z && Objects.equals(worldName, gb.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }
}
