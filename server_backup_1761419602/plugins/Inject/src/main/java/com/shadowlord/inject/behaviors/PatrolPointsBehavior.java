package com.shadowlord.inject.behaviors;

import com.shadowlord.inject.PatrolManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Moves the entity between a list of patrol points configured via PatrolManager.
 * Behavior queries PatrolManager for points for this entity's UUID.
 */
public class PatrolPointsBehavior implements Behavior {
  private final org.bukkit.plugin.Plugin plugin;
  private int currentIndex = 0;
  private int stuckTicks = 0;

  public PatrolPointsBehavior(org.bukkit.plugin.Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void onAttach(Entity entity) {
    // reset index when attached
    currentIndex = 0;
    stuckTicks = 0;
  }

  @Override
  public void tick(Entity entity) {
    if (!(entity instanceof LivingEntity)) return;
    LivingEntity le = (LivingEntity) entity;
    List<Location> points = PatrolManager.getInstance(plugin).getPointsFor(entity.getUniqueId());
    if (points == null || points.isEmpty()) return;

    // clamp index
    if (currentIndex >= points.size()) currentIndex = 0;
    Location target = points.get(currentIndex);
    if (target == null || target.getWorld() == null) return;

    // If at target (within small radius), advance to next
    double dist2 = le.getLocation().distanceSquared(target);
    if (dist2 < 1.0) {
      currentIndex = (currentIndex + 1) % points.size();
      stuckTicks = 0;
      return;
    }

    // Move toward target using small velocity changes
    Vector dir = target.toVector().subtract(le.getLocation().toVector()).normalize();
    double speed = 0.15; // tweak for desired pacing
    le.setVelocity(dir.multiply(speed));

    // fallback: if stuck for too long, teleport closer
    stuckTicks++;
    if (stuckTicks > 40) { // ~40 ticks ~ 2 seconds
      le.teleport(target.clone().add(0, 0.5, 0));
      stuckTicks = 0;
      currentIndex = (currentIndex + 1) % points.size();
    }
  }

  @Override
  public void onRemove(Entity entity) {}
}
