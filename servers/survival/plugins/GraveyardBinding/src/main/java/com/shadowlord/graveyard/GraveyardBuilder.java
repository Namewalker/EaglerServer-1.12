package com.shadowlord.graveyard;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class GraveyardBuilder {
  private static Material mat(String name) {
    Material m = Material.matchMaterial(name);
    return m != null ? m : Material.AIR;
  }

  public static void buildLevel(Location c, int level) {
    switch (level) {
      case 1: buildLevel1(c); break;
      case 2: buildLevel2(c); break;
      case 3: buildLevel3(c); break;
      case 4: buildLevel4(c); break;
      case 5: buildLevel5(c); break;
      default: buildLevel1(c); break;
    }
  }

  private static void clearArea(Location c, int radius) {
    World w = c.getWorld();
    int cx = c.getBlockX();
    int cy = c.getBlockY();
    int cz = c.getBlockZ();
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dz = -radius; dz <= radius; dz++) {
        for (int dy = 0; dy <= 5; dy++) {
          Block b = w.getBlockAt(cx + dx, cy + dy, cz + dz);
          if (b.getType() == Material.WATER || b.getType() == Material.AIR) continue;
          b.setType(Material.AIR);
        }
      }
    }
  }

  private static void placeCenterSign(Location c, String ownerName, int level) {
    World w = c.getWorld();
    Block center = w.getBlockAt(c.getBlockX(), c.getBlockY(), c.getBlockZ());
    Material mossy = mat("MOSSY_COBBLESTONE");
    if (mossy == Material.AIR) mossy = mat("COBBLESTONE");
    center.setType(mossy);

    Block signBlock = w.getBlockAt(c.getBlockX(), c.getBlockY() + 1, c.getBlockZ());
    Material standingSign = mat("SIGN_POST");
    if (standingSign == Material.AIR) standingSign = mat("WALL_SIGN");
    signBlock.setType(standingSign);
    try {
      org.bukkit.block.Sign sign = (org.bukkit.block.Sign) signBlock.getState();
      sign.setLine(0, "[Graveyard]");
      sign.setLine(1, ownerName == null ? "" : ownerName);
      sign.setLine(2, "Level " + level);
      sign.setLine(3, "Bound");
      sign.update();
    } catch (Throwable ignored) {}
  }

  private static void ring(World w, int cx, int cy, int cz, int r, Material mat) {
    for (int dx = -r; dx <= r; dx++) {
      for (int dz = -r; dz <= r; dz++) {
        if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
        w.getBlockAt(cx + dx, cy, cz + dz).setType(mat);
      }
    }
  }

  private static void buildLevel1(Location c) {
    clearArea(c, 2);
    World w = c.getWorld();
    int cx = c.getBlockX(), cy = c.getBlockY(), cz = c.getBlockZ();
    placeCenterSign(c, null, 1);
    Material cobweb = mat("COBWEB");
    if (cobweb == Material.AIR) cobweb = mat("WEB");
    // small decorative ring of cobwebs
    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
      if (dx == 0 && dz == 0) continue;
      w.getBlockAt(cx + dx, cy, cz + dz).setType(cobweb);
    }
  }

  private static void buildLevel2(Location c) {
    clearArea(c, 3);
    buildLevel1(c);
    World w = c.getWorld();
    int cx = c.getBlockX(), cy = c.getBlockY(), cz = c.getBlockZ();
    Material slab = mat("STONE_SLAB");
    if (slab == Material.AIR) slab = mat("STEP");
    Material torch = mat("TORCH");
    // neat 3x3 slab pad with torches at cardinal points
    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
      w.getBlockAt(cx + dx, cy -1, cz + dz).setType(slab);
    w.getBlockAt(cx + 2, cy, cz).setType(torch);
    w.getBlockAt(cx - 2, cy, cz).setType(torch);
    w.getBlockAt(cx, cy, cz + 2).setType(torch);
    w.getBlockAt(cx, cy, cz - 2).setType(torch);
    placeCenterSign(c, null, 2);
  }

  private static void buildLevel3(Location c) {
    clearArea(c, 4);
    buildLevel2(c);
    World w = c.getWorld();
    int cx = c.getBlockX(), cy = c.getBlockY(), cz = c.getBlockZ();
    Material brick = mat("STONEBRICK");
    if (brick == Material.AIR) brick = mat("STONE");
    ring(w, cx, cy, cz, 3, brick);
    // four short pillars
    for (int[] d : new int[][]{{3,3},{3,-3},{-3,3},{-3,-3}}) {
      w.getBlockAt(cx + d[0], cy +1, cz + d[1]).setType(brick);
      w.getBlockAt(cx + d[0], cy +2, cz + d[1]).setType(brick);
    }
    placeCenterSign(c, null, 3);
  }

  private static void buildLevel4(Location c) {
    clearArea(c, 5);
    buildLevel3(c);
    World w = c.getWorld();
    int cx = c.getBlockX(), cy = c.getBlockY(), cz = c.getBlockZ();
    Material obsidian = mat("OBSIDIAN");
    if (obsidian == Material.AIR) obsidian = mat("BEDROCK"); // fallback visual
    // corner obelisks
    for (int dx = -4; dx <= 4; dx += 8) {
      for (int dz = -4; dz <= 4; dz += 8) {
        for (int h = 0; h < 4; h++) w.getBlockAt(cx + dx, cy + h, cz + dz).setType(obsidian);
      }
    }
    // small fire ring
    try {
      Material neth = mat("NETHERRACK");
      Material fire = mat("FIRE");
      w.getBlockAt(cx + 2, cy, cz + 2).setType(neth);
      w.getBlockAt(cx + 2, cy + 1, cz + 2).setType(fire);
      w.getBlockAt(cx - 2, cy, cz - 2).setType(neth);
      w.getBlockAt(cx - 2, cy + 1, cz - 2).setType(fire);
      w.getBlockAt(cx + 2, cy , cz - 2).setType(neth);
      w.getBlockAt(cx + 2, cy + 1, cz - 2).setType(fire);
      w.getBlockAt(cx - 2, cy, cz + 2).setType(neth);
      w.getBlockAt(cx - 2, cy + 1, cz + 2).setType(fire);
    } catch (Throwable ignored) {}
    placeCenterSign(c, null, 4);
  }

  private static void buildLevel5(Location c) {
    clearArea(c, 7);
    buildLevel4(c);
    World w = c.getWorld();
    int cx = c.getBlockX(), cy = c.getBlockY(), cz = c.getBlockZ();
    Material gate = mat("IRON_BARS");
    if (gate == Material.AIR) gate = mat("FENCE");
    // grand arch: two pillars and a lintel
    for (int y = 0; y <= 3; y++) w.getBlockAt(cx -2, cy + y, cz -6).setType(gate);
    for (int y = 0; y <= 3; y++) w.getBlockAt(cx +2, cy + y, cz -6).setType(gate);
    for (int x = -2; x <= 2; x++) w.getBlockAt(cx + x, cy + 3, cz -6).setType(mat("STONE"));
    placeCenterSign(c, null, 5);
  }
}
