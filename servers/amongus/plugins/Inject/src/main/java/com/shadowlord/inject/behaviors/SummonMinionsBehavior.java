package com.shadowlord.inject.behaviors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * When the attached entity is damaged below a threshold or on interval,
 * it spawns a few weaker minions nearby.
 */
public class SummonMinionsBehavior implements Behavior {
  private final int minionCount;
  private final double healthThreshold; // fraction, e.g. 0.5 for 50%
  private final org.bukkit.plugin.Plugin plugin;
  private final Random rnd = new Random();
  private org.bukkit.event.Listener damageListener;

  public SummonMinionsBehavior(org.bukkit.plugin.Plugin plugin) {
    this(plugin, 2, 0.5);
  }

  public SummonMinionsBehavior(org.bukkit.plugin.Plugin plugin, int minionCount, double healthThreshold) {
    this.plugin = plugin;
    this.minionCount = Math.max(1, minionCount);
    this.healthThreshold = Math.max(0.0, Math.min(1.0, healthThreshold));
  }

  @Override
  public void onAttach(Entity entity) {
    // register a temporary damage listener that triggers minion spawn when health drops below threshold
    damageListener = new org.bukkit.event.Listener() {
      @org.bukkit.event.EventHandler
      public void onDamage(org.bukkit.event.entity.EntityDamageEvent e) {
        if (!e.getEntity().getUniqueId().equals(entity.getUniqueId())) return;
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) entity;
        double hp = le.getHealth() - e.getFinalDamage();
        double max = le.getMaxHealth();
        if (hp <= 0) return; // will die; let death behavior handle what you want
        if (hp / max <= healthThreshold) {
          spawnMinions(le);
          // after spawning once, unregister this listener to avoid repeated triggers
          org.bukkit.event.HandlerList.unregisterAll(this);
        }
      }
    };
    org.bukkit.Bukkit.getPluginManager().registerEvents(damageListener, plugin);

    // Also schedule periodic small chance to spawn minions (optional)
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!entity.isValid()) { cancel(); return; }
        // 1% chance each run (runs at registry tick rate), adjust as desired
        if (rnd.nextDouble() < 0.01) spawnMinions((LivingEntity) entity);
      }
    }.runTaskTimer(plugin, 40L, 100L); // start after 2s, every 5s
  }

  private void spawnMinions(LivingEntity parent) {
    Location base = parent.getLocation();
    World w = base.getWorld();
    for (int i = 0; i < minionCount; i++) {
      Location spawn = base.clone().add((rnd.nextDouble() - 0.5) * 2.5, 0.5, (rnd.nextDouble() - 0.5) * 2.5);
      // choose a weaker mob type; using zombie as example
      org.bukkit.entity.Entity e = w.spawnEntity(spawn, EntityType.ZOMBIE);
      if (e instanceof LivingEntity) {
        LivingEntity le = (LivingEntity) e;
        // reduce health and optionally set a custom name
        try { le.setMaxHealth(8.0); } catch (Throwable ignored) {}
        try { le.setHealth(Math.min(le.getMaxHealth(), 6.0)); } catch (Throwable ignored) {}
        le.setCustomName("Minion");
        le.setCustomNameVisible(false);
      }
    }
  }

  @Override
  public void tick(Entity entity) {
    // no per-tick behavior needed here (spawning driven by listeners/scheduler)
  }

  @Override
  public void onRemove(Entity entity) {
    if (damageListener != null) org.bukkit.event.HandlerList.unregisterAll(damageListener);
    // other cleanup is handled by scheduled task cancel logic when entity invalidates
  }
}
