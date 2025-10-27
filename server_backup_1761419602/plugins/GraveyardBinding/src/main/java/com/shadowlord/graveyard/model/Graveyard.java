package com.shadowlord.graveyard.model;

import org.bukkit.Location;

public class Graveyard {
  private final Location center;
  private int level;

  public Graveyard(Location center, int level) {
    this.center = center;
    this.level = Math.max(1, Math.min(5, level));
  }

  public Location getCenter() { return center; }
  public int getLevel() { return level; }
  public void setLevel(int level) { this.level = Math.max(1, Math.min(5, level)); }
}
