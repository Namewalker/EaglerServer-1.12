package com.shadowlord.undeadsiege.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SiegeRegion {
  private String worldName;
  private int x, y, z;
  private int size = 75;

  public SiegeRegion() {}

  public SiegeRegion(Location center, int size) {
    this.worldName = center.getWorld().getName();
    this.x = center.getBlockX();
    this.y = center.getBlockY();
    this.z = center.getBlockZ();
    this.size = size;
  }

  public Location getCenter() {
    World w = Bukkit.getWorld(worldName);
    if (w == null) return null;
    return new Location(w, x + 0.5, y, z + 0.5);
  }

  public int getHalfSize() { return size / 2; }
  public int getSize() { return size; }
  public String getWorldName() { return worldName; }
  public int getX() { return x; }
  public int getY() { return y; }
  public int getZ() { return z; }
}
