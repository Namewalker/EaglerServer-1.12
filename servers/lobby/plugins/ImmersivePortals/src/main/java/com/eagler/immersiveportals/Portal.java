package com.eagler.immersiveportals;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class Portal {
    private final int id;
    private final List<Location> blocks;
    private final World world;
    private final String name;
    public ArmorStand nameTag;

    public Portal(int id, List<Location> blocks, World world, String name) {
        this.id = id;
        this.blocks = blocks;
        this.world = world;
        this.name = name;
    }

    public void spawnNameTag(JavaPlugin plugin) {
        Location center = getCenter();
        nameTag = world.spawn(center.add(0, 2, 0), ArmorStand.class, stand -> {
            stand.setCustomName(name);
            stand.setCustomNameVisible(true);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
        });
    }

    public void setNameTagVisible(boolean visible) {
        if (nameTag != null) {
            nameTag.setCustomNameVisible(visible);
        }
    }

    public String getName() { return name; }

    public Location getCenter() {
        double x = 0, y = 0, z = 0;
        for (Location loc : blocks) {
            x += loc.getX();
            y += loc.getY();
            z += loc.getZ();
        }
        int size = blocks.size();
        return new Location(world, x / size, y / size, z / size);
    }

    public int getId() { return id; }
    public List<Location> getBlocks() { return blocks; }
}
