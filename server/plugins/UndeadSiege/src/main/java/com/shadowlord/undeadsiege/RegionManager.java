package com.shadowlord.undeadsiege;

import com.shadowlord.undeadsiege.model.SiegeRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class RegionManager {
  private final UndeadSiegePlugin plugin;
  private SiegeRegion region;

  public RegionManager(UndeadSiegePlugin plugin) {
    this.plugin = plugin;
    loadRegionFromConfig();
  }

  public void saveRegionToConfig(SiegeRegion r) {
    if (r == null) {
      plugin.getConfig().set("siege", null);
      plugin.saveConfig();
      region = null;
      return;
    }
    plugin.getConfig().set("siege.center.world", r.getWorldName());
    plugin.getConfig().set("siege.center.x", r.getX());
    plugin.getConfig().set("siege.center.y", r.getY());
    plugin.getConfig().set("siege.center.z", r.getZ());
    plugin.getConfig().set("siege.size", r.getSize());
    plugin.saveConfig();
    region = r;
  }

  private void loadRegionFromConfig() {
    if (!plugin.getConfig().isSet("siege.center.world")) { region = null; return; }
    World w = Bukkit.getWorld(plugin.getConfig().getString("siege.center.world"));
    int x = plugin.getConfig().getInt("siege.center.x");
    int y = plugin.getConfig().getInt("siege.center.y");
    int z = plugin.getConfig().getInt("siege.center.z");
    int size = plugin.getConfig().getInt("siege.size", 100);
    if (w == null) { region = null; return; }
    region = new SiegeRegion(new Location(w, x, y, z), size);
  }

  public SiegeRegion getRegion() { return region; }

  // Build border: alternating black/yellow concrete at ground level and barrier column below it
  public void buildBorder(SiegeRegion r) {
    if (r == null) return;
    Location c = r.getCenter();
    World w = c.getWorld();
    int half = r.getHalfSize();
    int groundY = r.getY();
    boolean toggle;
    for (int dx = -half; dx <= half; dx++) {
      toggle = (dx % 2 == 0);
      for (int dz = -half; dz <= half; dz++) {
        if (Math.abs(dx) != half && Math.abs(dz) != half) continue;
        int bx = r.getX() + dx;
        int bz = r.getZ() + dz;
        Block b = w.getBlockAt(bx, groundY, bz);
        if ((Math.abs(dx) == half && Math.abs(dz) <= half) || (Math.abs(dz) == half && Math.abs(dx) <= half)) {
          try {
            b.setType(Material.CONCRETE);
            b.setData((byte)(toggle ? 15 : 4)); // 15=black, 4=yellow
          } catch (Throwable t) {
            b.setType(Material.WOOL);
            b.setData((byte)(toggle ? 15 : 4));
          }
          // place barrier column descending three blocks below the concrete to stop climbing/placing
          for (int dy = 1; dy >= -3; dy--) {
            Block barrierBlock = w.getBlockAt(bx, groundY + dy, bz);
            barrierBlock.setType(Material.BARRIER);
          }
          toggle = !toggle;
        }
      }
    }
  }

  public void removeBorder(SiegeRegion r) {
    if (r == null) return;
    Location c = r.getCenter();
    World w = c.getWorld();
    int half = r.getHalfSize();
    int groundY = r.getY();
    for (int dx = -half; dx <= half; dx++) {
      for (int dz = -half; dz <= half; dz++) {
        if (Math.abs(dx) != half && Math.abs(dz) != half) continue;
        int bx = r.getX() + dx;
        int bz = r.getZ() + dz;
        // remove barrier column
        for (int dy = 1; dy >= -3; dy--) {
          Block bAbove = w.getBlockAt(bx, groundY + dy, bz);
          if (bAbove.getType() == Material.BARRIER) bAbove.setType(Material.AIR);
        }
        Block b = w.getBlockAt(bx, groundY, bz);
        Material mt = b.getType();
        if (mt == Material.CONCRETE || mt == Material.WOOL) {
          b.setType(Material.AIR);
        }
      }
    }
  }
}
