package com.shadowlord.undeadsiege;

import com.shadowlord.undeadsiege.model.Wave;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Random;

public class MobSpawner {
  private final UndeadSiegePlugin plugin;
  private final Random rnd = new Random();

  public MobSpawner(UndeadSiegePlugin plugin) { this.plugin = plugin; }

  public void spawnWave(Location center, Wave wave, int multiplier) {
    for (Map.Entry<EntityType, Integer> e : wave.getSpawns().entrySet()) {
      EntityType type = e.getKey();
      int count = e.getValue() * Math.max(1, multiplier);
      for (int i = 0; i < count; i++) {
        Location spawn = randomRingLocation(center);
        Entity ent = center.getWorld().spawnEntity(spawn, type);
        ent.setMetadata("siege_mob", new FixedMetadataValue((Plugin)plugin, true));
        if (ent instanceof LivingEntity) {
          LivingEntity le = (LivingEntity) ent;
          le.setNoDamageTicks(0);
          if (type == EntityType.ZOMBIE) {
            le.setCustomName("Grunt");
            le.setCustomNameVisible(false);
          } else if (type == EntityType.SKELETON) {
            le.setCustomName("Watcher");
            le.setCustomNameVisible(false);
          }
        }
        // 50% chance to drop a bedrock token at the spawn location (players can pick up)
        if (rnd.nextBoolean()) {
          ItemStack drop = new ItemStack(Material.BEDROCK, 1);
          org.bukkit.inventory.meta.ItemMeta im = drop.getItemMeta();
          im.setDisplayName("Soul Token");
          im.setLore(java.util.Arrays.asList("Use this to upgrade graveyards.", "Right-click a graveyard sign to spend."));
          drop.setItemMeta(im);
          center.getWorld().dropItemNaturally(ent.getLocation(), drop);
        }
      }
    }
  }

  private Location randomRingLocation(Location center) {
    double minR = 20.0;
    double maxR = 35.0;
    double r = minR + rnd.nextDouble() * (maxR - minR);
    double angle = rnd.nextDouble() * Math.PI * 2;
    double dx = Math.cos(angle) * r;
    double dz = Math.sin(angle) * r;
    Location l = center.clone().add(dx, 0, dz);
    int y = center.getWorld().getHighestBlockYAt(l) + 1;
    l.setY(y);
    return l;
  }
}
