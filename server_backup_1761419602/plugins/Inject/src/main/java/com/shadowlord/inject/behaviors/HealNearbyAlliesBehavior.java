package com.shadowlord.inject.behaviors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Bukkit;

import java.util.Collection;

/**
 * Periodically heals nearby allied mobs (same entity type) within radius.
 * Lightweight: heals a small amount every tick interval.
 */
public class HealNearbyAlliesBehavior implements Behavior {
  private final double radius;
  private final double healAmount;
  private final int tickInterval; // number of registry ticks between heals
  private int counter = 0;

  public HealNearbyAlliesBehavior() {
    this(6.0, 2.0, 10); // default: 6 block radius, 2 HP, every 10 ticks
  }

  public HealNearbyAlliesBehavior(double radius, double healAmount, int tickInterval) {
    this.radius = radius;
    this.healAmount = healAmount;
    this.tickInterval = Math.max(1, tickInterval);
  }

  @Override
  public void onAttach(Entity entity) {}

  @Override
  public void tick(Entity entity) {
    if (!(entity instanceof LivingEntity)) return;
    counter++;
    if (counter < tickInterval) return;
    counter = 0;

    LivingEntity le = (LivingEntity) entity;
    Location loc = le.getLocation();
    Collection<org.bukkit.entity.Entity> nearby = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
    for (org.bukkit.entity.Entity e : nearby) {
      if (!(e instanceof LivingEntity)) continue;
      LivingEntity ally = (LivingEntity) e;
      // only heal same creature type (allies) and avoid players
      if (ally.getType() == le.getType() && !(ally instanceof Player)) {
        double newHp = Math.min(ally.getMaxHealth(), ally.getHealth() + healAmount);
        try { ally.setHealth(newHp); } catch (Throwable ignored) {}
      }
    }
  }

  @Override
  public void onRemove(Entity entity) {
    // nothing to cleanup
  }
}
